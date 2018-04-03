package dna.controller;

import dna.constants.DocType;
import dna.constants.ServerConstant;
import dna.entity.top.DocInfo;
import dna.origins.commons.EhcacheUtils;
import dna.origins.commons.ResObject;
import dna.origins.commons.ResultCode;
import dna.origins.commons.ServiceException;
import dna.origins.util.authorization.CookieUtils;
import dna.origins.util.authorization.TokenUtils;
import dna.origins.util.constants.EhcacheConstant;
import dna.origins.util.constants.JWTConstant;
import dna.persistence.factory.ContextUtil;
import dna.persistence.regulate.FTPClientProcessor;
import dna.persistence.shiro.UserRoleToken;
import dna.persistence.template.Context;
import dna.top.service.DocInfoService;
import dna.top.service.UploadService;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dna.persistence.shiro.UserRoleToken.CID_BASE;

@CrossOrigin(origins = "*", exposedHeaders = {JWTConstant.TOKEN_HEAD_NAME})
@Controller
public class UploadManager {

    @Autowired
    private FTPClientProcessor ftpClientProcessor;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private DocInfoService docInfoService;

    @PostMapping(value = "auth/upload")
    public ResponseEntity uploadFile(@RequestParam MultipartFile[] files, @RequestParam int rank, @RequestParam int docType) throws Exception {
        UserRoleToken urt = UserRoleToken.getCurrentUrt();
        List<DocInfo> docInfos = new ArrayList<>();
        List<Integer> docIds = new ArrayList<>();
        for (MultipartFile file : files) {
            String remotePath = "/" + (StringUtils.isBlank(urt.getCompanyCode()) ? CID_BASE : urt.getCompanyCode()) + "/" + DocType.getName(docType);
            String remoteName = new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + RandomStringUtils.randomNumeric(2);
            if (ftpClientProcessor.uploadFile(remotePath, remoteName, file.getInputStream())) {
                DocInfo doc = new DocInfo();
                doc.setFileName(file.getOriginalFilename());
                doc.setFileSize(file.getSize());
                doc.setContentType(file.getContentType());
                doc.setRank(rank == 0 ? 1 : rank);
                doc.setDocType(docType);
                doc.setRemoteName(remoteName);
                doc.setRemotePath(remotePath);
                doc.setCompanyCode(urt.getCompanyCode());
                doc.setUserId(urt.getUserId());
                doc.setStatus(1);
                doc.setCreateTime(new Date());
                docInfos.add(doc);
            }
        }
        docInfoService.addDocInfo(docInfos);
        docInfos.forEach(d -> docIds.add(d.getId()));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject<>(ResultCode.REQUESST_SUCCESS, docIds));
    }

    @GetMapping(value = ServerConstant.PATH_DOWNLOAD + "/{docId}")
    public void downloadFile(@PathVariable("docId") Integer docId, HttpServletRequest request, HttpServletResponse response) throws Exception {

        Cookie cookie_uuid = CookieUtils.getCookieByName(request, JWTConstant.UUID_COOIKE_NAME);
        Cookie cookie_tk = CookieUtils.getCookieByName(request, JWTConstant.TOKEN_COOIKE_NAME);
        Boolean cookieEnable = cookie_tk != null && cookie_uuid != null;
        String jwtToken = cookieEnable ? cookie_tk.getValue() : request.getHeader(JWTConstant.TOKEN_HEAD_NAME);
        String uuid = cookieEnable ? cookie_uuid.getValue() : request.getHeader(JWTConstant.UUID_HEAD_NAME);
        if (StringUtils.isNotBlank(uuid) && StringUtils.isNotBlank(jwtToken)) {
            Key key = (Key) EhcacheUtils.instance().get(EhcacheConstant.KEY_CACHE_NAME, uuid);
            if (key != null) {
                Claims claims = TokenUtils.getClaims(jwtToken, key);
                if (claims != null) {
                    String urt_key = claims.get(TokenUtils.URT, String.class);
                    UserRoleToken urt = (UserRoleToken) EhcacheUtils.instance().get(EhcacheConstant.TOKEN_CACHE_NAME, urt_key);
                    String authIP = claims.getSubject();
                    String currentIP = TokenUtils.getIpAddr(request);
                    if (urt != null && urt.isValid() && ((JWTConstant.LOCALHOST_IP.contains(currentIP) && JWTConstant.LOCALHOST_IP.contains(authIP)) || currentIP.equals(authIP))) {
                        ContextUtil.put(Context.URT, urt);
                    }
                }
            }
        }

        DocInfo docInfo = docInfoService.getDocInfo(docId);
        if (docInfo == null) {
            throw new ServiceException(ServiceException.ENTITIY_NOT_FOUND, "docInfo[" + docId + "] not found");
        }
        response.setHeader("Cache-Control", "max-age=86400,must-revalidate");
        //注意Content-Disposition格式
        response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\"; filename*=utf-8''%s",
                URLEncoder.encode(docInfo.getFileName(), "UTF-8"), URLEncoder.encode(docInfo.getFileName(), "UTF-8")));
        response.setContentType(docInfo.getContentType());
        response.setContentLengthLong(docInfo.getFileSize());
        //必须先设置好响应头再调用输出流
        uploadService.downloadRemoteFile(docId, response.getOutputStream());
        response.getOutputStream().flush();
        response.getOutputStream().close();
        //responseEntity无法处理大文件
        //return ResponseEntity.status(HttpStatus.OK).headers(headers).contentLength(docInfo.getFileSize()).contentType(MediaType.valueOf(docInfo.getContentType())).body(new InputStreamResource(inputStream));
    }
}
