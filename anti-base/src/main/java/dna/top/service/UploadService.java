package dna.top.service;

import dna.constants.DocContentType;
import dna.constants.DocType;
import dna.constants.RankConstant;
import dna.entity.top.DocInfo;
import dna.origins.commons.ServiceException;
import dna.persistence.factory.DAOFactory;
import dna.persistence.regulate.FTPClientProcessor;
import dna.persistence.shiro.UserRoleToken;
import dna.top.dao.DocInfoDAO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static dna.persistence.shiro.UserRoleToken.CID_BASE;

@Service
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FTPClientProcessor ftpClientProcessor;

    @Transactional
    public Integer UploadLocalFile(@NotNull File file, @NotNull DocType docType, @NotNull String companyCode, @NotNull Integer range) {
        UserRoleToken urt = UserRoleToken.getCurrentUrt();
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);

            DocInfo doc = new DocInfo();
            String remotePath = "/" + (StringUtils.isBlank(urt.getCompanyCode()) ? CID_BASE : urt.getCompanyCode()) + "/" + docType.getName();
            String remoteName = new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + RandomStringUtils.randomNumeric(2);
            boolean result = ftpClientProcessor.uploadFile(remotePath, remoteName, inputStream);
            if (!result) {
                throw new ServiceException(ServiceException.SERVICE_ERROR, "文件上传失败，请稍后重试！");
            }
            DocInfoDAO docInfoDAO = DAOFactory.getDAO(DocInfoDAO.class);
            doc.setFileName(file.getName());
            doc.setFileSize(file.length());
            String[] fileFix = file.getName().split("\\.");
            String suffix = fileFix.length > 1 ? fileFix[fileFix.length - 1] : "unknown";
            String contentType = DocContentType.getType(suffix) == null ? DocContentType.getType("unknown") : DocContentType.getType(suffix);
            doc.setContentType(contentType);
            doc.setDocType(docType.getId());
            doc.setCompanyCode(companyCode);
            doc.setRank(range);
            doc.setRemoteName(remoteName);
            doc.setRemotePath(remotePath);
            doc.setUserId(urt.getUserId());
            doc.setStatus(1);
            doc.setCreateTime(new Date());
            docInfoDAO.save(doc);
            return doc.getId();
        } catch (FileNotFoundException e) {
            logger.error("", e);
            throw new ServiceException(ServiceException.SERVICE_ERROR, "文件不存在，请稍后重试！");
        }
    }

    public void downloadRemoteFile(int docId, OutputStream outputStream) throws Exception {
        DocInfoDAO docInfoDAO = DAOFactory.getDAO(DocInfoDAO.class);
        DocInfo docInfo = docInfoDAO.get(docId);
        UserRoleToken urt = UserRoleToken.getCurrentUrt();
        switch (docInfo.getRank()) {
            case RankConstant.RANK_OWN:
                if (urt == null || !urt.getUserId().equals(docInfo.getUserId())) {
                    throw new AuthorizationException("invalid authorization for doc[" + docId + "]: rank is " + docInfo.getRank());
                }
                break;
            case RankConstant.RANK_COMPANY:
                if (urt == null || !ObjectUtils.nullSafeEquals(urt.getCompanyCode(), docInfo.getCompanyCode())) {
                    throw new AuthorizationException("invalid authorization for doc[" + docId + "]: rank is " + docInfo.getRank());
                }
                break;
            default:
                break;
        }
        ftpClientProcessor.downloadFile(docInfo.getRemotePath(), docInfo.getRemoteName(), outputStream);
    }

    /**
     * 读取文本文件内容
     *
     * @param docId：文件id
     * @return result：文件内容
     */
    public List<String> readFileToString(Integer docId) {
        DocInfoDAO docInfoDAO = DAOFactory.getDAO(DocInfoDAO.class);
        DocInfo docInfo = docInfoDAO.get(docId);
        Pattern pattern = Pattern.compile("[0-9]*");
        List<String> result = new ArrayList<>();

        FTPClient ftpClient = ftpClientProcessor.getFTPClient();
        InputStream in;
        BufferedReader reader = null;
        try {

            ftpClient.changeWorkingDirectory(docInfo.getRemotePath());//转移到FTP服务器目录

            in = ftpClient.retrieveFileStream(docInfo.getRemoteName());

            reader = new BufferedReader(new InputStreamReader(in));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                if (tempString.length() > 32 || !pattern.matcher(tempString).matches()) {
                    throw new ServiceException(ServiceException.SERVICE_ERROR, "文件内数码格式错误");
                }

                result.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            ftpClientProcessor.returnFTPClient(ftpClient);
        }

        return result;
    }
}
