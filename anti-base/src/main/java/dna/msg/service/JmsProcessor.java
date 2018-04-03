package dna.msg.service;

import dna.config.ThirdApiConfig;
import dna.constants.StatusConstant;
import dna.entity.msg.MqRecord;
import dna.msg.dao.MqRecordDAO;
import dna.origins.commons.CallBack;
import dna.origins.commons.MessageType;
import dna.persistence.factory.DAOFactory;
import dna.persistence.shiro.ApplicationToken;
import dna.persistence.shiro.UserRoleToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.util.Date;
import java.util.List;

/**
 * 消息队列处理器
 *
 * @author zhangsl
 */
@Component
public class JmsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(JmsProcessor.class);

    @Autowired
    private JmsProcessor jmsProcessor;

    @Autowired
    private ThirdApiConfig thirdApiConfig;

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private Queue securityQueryRecordQueue;

    @Autowired
    private Queue billUploadRecordQueue;

    @Autowired
    private Queue redPackRecordQueue;

    public void receiveMsgRecord(Integer mqRecordId, CallBack action) {
        MqRecordDAO mqRecordDAO = DAOFactory.getDAO(MqRecordDAO.class);
        MqRecord mqRecord = mqRecordDAO.get(mqRecordId);
        if (mqRecord == null) {
            logger.error("can not get record by msg[{}]", mqRecordId);
        } else if (jmsProcessor.checkMessage(mqRecord)) {
            action.execute(mqRecord);
            mqRecordDAO.update(mqRecord);
        }
    }

    /**
     * 消息已读
     *
     * @param mqRecord
     * @return
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean checkMessage(final MqRecord mqRecord) {
        MqRecordDAO mqRecordDAO = DAOFactory.getDAO(MqRecordDAO.class);
        if (StringUtils.isNotBlank(mqRecord.getCompanyCode()) && mqRecord.getStatus() != StatusConstant.STATUS_COMPLETE) {//冥等
            mqRecord.setStatus(StatusConstant.STATUS_STARTED);
            mqRecord.setUpdateTime(new Date());
            mqRecordDAO.update(mqRecord);
            ApplicationToken token = new ApplicationToken(mqRecord.getCompanyCode());//注入系统权限
            SecurityUtils.getSubject().login(token);
            return true;
        } else {
            if (mqRecord.getStatus() != StatusConstant.STATUS_COMPLETE) {
                mqRecord.setStatus(StatusConstant.STATUS_FAILED);
                mqRecord.setUpdateTime(new Date());
                mqRecordDAO.update(mqRecord);
            }
            logger.error("invalid record for msg[{}]", mqRecord.getId());
            return false;
        }
    }


    public void sendRecordMsgWithExtra(String cid, MessageType type, Long recordId, String extraArg1, String extraArg2, String extraArgs) {
        cid = StringUtils.isBlank(cid) ? UserRoleToken.CID_BASE : cid.trim();
        MqRecordDAO mqRecordDAO = DAOFactory.getDAO(MqRecordDAO.class);
        MqRecord mqRecord = new MqRecord(cid, type.getId(), recordId, extraArg1, extraArg2, extraArgs);
        mqRecordDAO.save(mqRecord);
        if (!jmsProcessor.sendMsgByType(type, mqRecord)) {
            mqRecord.setStatus(StatusConstant.STATUS_STARTED);//发送失败，等待重发
            mqRecordDAO.update(mqRecord);
        }
    }

    public void sendRecordMsgWithExtra(String cid, MessageType type, Long recordId, String extraArg1, String extraArg2) {
        sendRecordMsgWithExtra(cid, type, recordId, extraArg1, extraArg2, null);
    }

    public void sendRecordMsgWithExtra(String cid, MessageType type, Long recordId, String extraArgs) {
        sendRecordMsgWithExtra(cid, type, recordId, null, null, extraArgs);
    }

    public void sendRecordMsg(String cid, MessageType type, Long recordId) {
        sendRecordMsgWithExtra(cid, type, recordId, null, null, null);
    }

    /**
     * 每隔一小时再次发送未处理成功消息
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 1000)
    public void refreshStartedMsg() {
        MqRecordDAO mqRecordDAO = DAOFactory.getDAO(MqRecordDAO.class);
        List<MqRecord> unFinishedMsg = mqRecordDAO.findByStatus(StatusConstant.STATUS_STARTED);
        unFinishedMsg.forEach(msg -> {
            jmsProcessor.sendMsgByType(MessageType.getType(msg.getType()), msg);
        });
        if (unFinishedMsg.size() > 0) {
            logger.info("refreshStartedMsg success,num:{}", unFinishedMsg.size());
        }
    }

    /**
     * 发送至消息队列，不管是否成功
     *
     * @param type
     * @param mqRecord
     */
    public boolean sendMsgByType(MessageType type, MqRecord mqRecord) {
        try {
            switch (type) {
                case SC_QUERY:
                    jmsMessagingTemplate.convertAndSend(securityQueryRecordQueue, mqRecord.getId());
                    break;
                case BILL_UPLOAD:
                    jmsMessagingTemplate.convertAndSend(billUploadRecordQueue, mqRecord.getId());
                    break;
                case SEND_RED_PACK_SUCCESS:
                    jmsMessagingTemplate.convertAndSend(redPackRecordQueue, mqRecord.getId());
                    break;
                default:
                    logger.error("unDefined type[{}] to sendMsg", mqRecord == null ? null : mqRecord.getId());
                    break;
            }
        } catch (Exception e) {
            logger.error("mqRecord[{}] send failed,cause:", mqRecord == null ? null : mqRecord.getId(), e);
            return false;
        }
        return true;
    }
}
