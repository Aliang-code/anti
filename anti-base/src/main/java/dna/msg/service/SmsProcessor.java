package dna.msg.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.mns.model.Message;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import dna.constants.StatusConstant;
import dna.entity.msg.SmsRecord;
import dna.msg.dao.SmsRecordDAO;
import dna.origins.commons.SmsType;
import dna.origins.util.toolbar.AliyunUtils;
import dna.persistence.factory.DAOFactory;
import dna.persistence.shiro.UserRoleToken;
import dna.persistence.template.EnvConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SmsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SmsProcessor.class);
    private static final Cache<String, String> verCodeCache = CacheBuilder.newBuilder()
            //设置cache的初始大小为10，要合理设置该值
            .initialCapacity(500)
            //最大数量
            .maximumSize(1000)
            //设置并发数为5，即同一时间最多只能有10个线程往cache执行写入操作
            .concurrencyLevel(10)
            //设置cache中的数据在写入之后的存活时间为300秒
            .expireAfterWrite(300, TimeUnit.SECONDS)
            //构建cache实例
            .build();

    @Autowired
    private SmsProcessor smsProcessor;

    public Boolean sendVerifySms(String cid, String phoneNumber) {
        String verCode = RandomStringUtils.randomNumeric(6);
        Map<String, String> params = new ImmutableMap.Builder<String, String>().
                put("code", verCode)
                .build();
        verCodeCache.put(phoneNumber, verCode);
        return sendSmsRecord(cid, SmsType.VERIFY_CODE, phoneNumber, params, null);
    }

    public Boolean checkVerCode(String phoneNumber, String verCode) {
        String code = verCodeCache.getIfPresent(phoneNumber);
        if (code != null && code.equals(verCode)) {
            removeVerCode(phoneNumber);
            return true;
        } else {
            return false;
        }
    }

    public void removeVerCode(String phoneNumber) {
        verCodeCache.invalidate(phoneNumber);
        verCodeCache.cleanUp();
    }

    public boolean sendSmsRecord(String cid, SmsType type, String phoneNumber, Map<String, String> params, String extra) {
        boolean result = false;
        SmsRecordDAO smsRecordDAO = DAOFactory.getDAO(SmsRecordDAO.class);
        String content = type.getTempContent();
        for (String k : params.keySet()) {
            content = content.replace("${" + k + "}", params.get(k));
        }
        if (EnvConfig.isDebug()) {
            result = true;
            logger.info("sms content:{}", content);
        } else {
            SmsRecord smsRecord = new SmsRecord(cid == null ? UserRoleToken.CID_BASE : cid, type.getId(), phoneNumber, content, extra);
            try {
                SendSmsResponse response = AliyunUtils.sendSms(type.getTempCode(), AliyunUtils.DEFAULT_SIGN_NAME, phoneNumber, params, extra);
                if (response.getBizId() != null) {
                    smsRecord.setBizId(response.getBizId());
                    if (response.getCode() != null && response.getCode().equals("OK")) {
                        result = true;
                        smsRecord.setStatus(StatusConstant.STATUS_STARTED);
                    } else {
                        smsRecord.setStatus(StatusConstant.STATUS_FAILED);
                    }
                    smsRecordDAO.save(smsRecord);
                } else {
                    logger.error("can not resolve sendVerifySms response[{}]", JSON.toJSONString(response));
                }
            } catch (Exception e) {
                logger.error("sendVerifySms error:", e);
                result = false;
            }
        }
        return result;
    }

    public void startMessageListener() {
        if (!EnvConfig.isDebug()) {
            try {
                AliyunUtils.openReportListener((message) -> {
                    //消息的几个关键值
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                System.out.println("message receiver time from mns:" + format.format(new Date()));
//                System.out.println("message handle: " + message.getReceiptHandle());
//                System.out.println("message body: " + message.getMessageBodyAsString());
//                System.out.println("message id: " + message.getMessageId());
//                System.out.println("message dequeue count:" + message.getDequeueCount());
//                System.out.println("Thread:" + Thread.currentThread().getName());
                    try {
                        smsProcessor.handleSmsMessage(message);
                    } catch (Exception e) {
                        //您自己的代码部分导致的异常，应该return false,这样消息不会被delete掉，而会根据策略进行重推
                        logger.error("can not dealMessage[{}]", message.toString(), e);
                        return false;
                    }
                    //消息处理成功，返回true, SDK将调用MNS的delete方法将消息从队列中删除掉
                    return true;
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            logger.info("start SmsMessageListener success");
        }
    }

    public void handleSmsMessage(Message message) {
        JSONObject body = JSONObject.parseObject(message.getMessageBodyAsString());
        String bizId = body.getString("biz_id");
        boolean result = body.getBooleanValue("success");
        SmsRecordDAO smsRecordDAO = DAOFactory.getDAO(SmsRecordDAO.class);
        SmsRecord smsRecord = smsRecordDAO.getByBizId(bizId);
        if (smsRecord != null) {
            if (result) {
                smsRecord.setStatus(StatusConstant.STATUS_COMPLETE);
            } else {
                String errCode = body.getString("err_code");
                String errMsg = body.getString("err_msg");
                smsRecord.setErrMsg(errCode + ":" + errMsg);
                smsRecord.setStatus(StatusConstant.STATUS_FAILED);
                smsRecord.setUpdateTime(new Date());
            }
            smsRecordDAO.update(smsRecord);
        }
    }
}
