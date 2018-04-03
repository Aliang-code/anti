package dna.msg.service;

import com.alibaba.fastjson.JSONObject;
import dna.ac.dao.SecurityQueryRecordDAO;
import dna.ac.service.SecurityQueryRecordService;
import dna.config.ThirdApiConfig;
import dna.constants.StatusConstant;
import dna.entity.ac.SecurityQueryRecord;
import dna.entity.market.RedPackRecord;
import dna.entity.msg.MqRecord;
import dna.entity.pda.BillUploadRecord;
import dna.entity.top.WxAccountInfo;
import dna.market.dao.RedPackRecordDAO;
import dna.market.service.RedPackManageService;
import dna.msg.dao.MqRecordDAO;
import dna.origins.commons.CallBack;
import dna.origins.commons.JmsConstants;
import dna.origins.commons.MessageType;
import dna.origins.commons.ResObject;
import dna.origins.util.constants.PlatformConstant;
import dna.pda.dao.BillUploadRecordDAO;
import dna.pda.service.PDAService;
import dna.persistence.factory.DAOFactory;
import dna.persistence.shiro.ApplicationToken;
import dna.persistence.shiro.UserRoleToken;
import dna.top.service.WxProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jms.annotation.JmsListener;
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
    private SecurityQueryRecordService securityQueryRecordService;

    @Autowired
    private PDAService pdaService;

    @Autowired
    private WxProcessor wxProcessor;

    @Autowired
    private RedPackManageService redPackManageService;

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private Queue securityQueryRecordQueue;

    @Autowired
    private Queue billUploadRecordQueue;

    @Autowired
    private Queue redPackRecordQueue;

    @JmsListener(destination = JmsConstants.QUEUE_RECORD_SECURITY_QUERY, containerFactory = "jmsListenerContainerQueue")
    public void receiveScQueryRecord(Integer mqRecordId) {
        receiveMsgRecord(mqRecordId, (args) -> {
            MqRecord mqRecord = (MqRecord) args[0];
            String cid = UserRoleToken.getCurrentUrt().getCompanyCode();
            SecurityQueryRecordDAO securityQueryRecordDAO = DAOFactory.getDAOByUrt(SecurityQueryRecordDAO.class);
            SecurityQueryRecord scRecord = securityQueryRecordDAO.get(mqRecord.getRecordId());
            if (scRecord == null || scRecord.getStatus() == StatusConstant.STATUS_COMPLETE) {
                mqRecord.setStatus(scRecord == null ? StatusConstant.STATUS_FAILED : StatusConstant.STATUS_COMPLETE);
                logger.error("can not get securityQueryRecord[{}] or it has been resolved for mqRecord[{}]", scRecord == null ? null : scRecord.getId(), mqRecord.getId());
            } else {
                try {
                    if (securityQueryRecordService.handleScQueryRecordMsg(cid, scRecord)) {
                        mqRecord.setStatus(StatusConstant.STATUS_COMPLETE);
                    } else {
                        logger.error("can not handle securityQueryRecord[{}]", scRecord.getId());
                    }
                } catch (Exception e) {
                    logger.error("handle securityQueryRecord[{}] error cause:", scRecord.getId(), e);
                }
            }
        });
    }

    @JmsListener(destination = JmsConstants.QUEUE_RECORD_BILL_UPLOAD, containerFactory = "jmsListenerContainerQueue")
    public void receiveBillUploadRecord(Integer mqRecordId) {
        receiveMsgRecord(mqRecordId, (args) -> {
            MqRecord mqRecord = (MqRecord) args[0];
            BillUploadRecordDAO billUploadRecordDAO = DAOFactory.getDAOByUrt(BillUploadRecordDAO.class);
            BillUploadRecord buRecord = billUploadRecordDAO.get(mqRecord.getRecordId());
            if (buRecord == null || buRecord.getStatus() == StatusConstant.STATUS_COMPLETE) {//直接丢弃
                mqRecord.setStatus(buRecord == null ? StatusConstant.STATUS_FAILED : StatusConstant.STATUS_COMPLETE);
                logger.error("can not get billUploadRecord[{}] or it has been resolved for mqRecord[{}]", buRecord == null ? null : buRecord.getId(), mqRecord.getId());
            } else {
                try {
                    if (pdaService.handleBillUploadMsg(buRecord)) {
                        mqRecord.setStatus(StatusConstant.STATUS_COMPLETE);
                    } else {
                        logger.error("can not handle billUploadRecord[{}]", buRecord.getId());
                    }
                } catch (Exception e) {
                    logger.error("handle billUploadRecord[{}] error cause:", buRecord.getId(), e);
                }
            }
        });
    }

    @JmsListener(destination = JmsConstants.TOPIC_WX_AUTH_SUCCESS, containerFactory = "jmsListenerContainerTopic")
    public void receiveWxAuthSuccess(MqRecord mqRecord) {
        if (mqRecord != null && mqRecord.getId() != null && mqRecord.getExtraArg1() != null && mqRecord.getExtraArg2() != null && mqRecord.getExtraArgs() != null) {
            String platformCode = mqRecord.getExtraArg1();
            String companyCode = mqRecord.getExtraArg2();
            String authorizerData = mqRecord.getExtraArgs();
            if (thirdApiConfig.getAnti_platform_code_wjzm().equals(platformCode)) {
                try {
                    WxAccountInfo wxAccountInfo = JSONObject.parseObject(authorizerData, WxAccountInfo.class);
                    if (wxProcessor.handleWxAuthSuccessMsg(companyCode, wxAccountInfo)) {
                        logger.info("企业[{}]公众号[{}]授权消息处理成功！", companyCode, wxAccountInfo.getAppId());

                        String url = thirdApiConfig.getAnti_platform_url() + "wx/auth/confirmAuthMsg";
                        JSONObject postData = new JSONObject();
                        postData.put(PlatformConstant.PARAM_MQRECORD_ID, mqRecord.getId());
                        ResObject<ResObject<String>> result = wxProcessor.postAntiRequest(url, postData, new ParameterizedTypeReference<ResObject<String>>() {
                        });
                        if (!result.isSuccess()) {
                            logger.error("wxAuthSuccessMsg[{}] callback failed", mqRecord.getId());
                        }
                    } else {
                        logger.error("can not handle wxAuthSuccessMsg[{}]", JSONObject.toJSONString(mqRecord));
                    }
                } catch (Exception e) {
                    logger.error("handle wxAuthSuccessMsg[{}] error cause:", JSONObject.toJSONString(mqRecord), e);
                }
            }
        } else {
            logger.error("wxAuthSuccessMsg[{}] is invalid", JSONObject.toJSONString(mqRecord));
        }
    }

    @JmsListener(destination = JmsConstants.QUEUE_RECORD_RED_PACK, containerFactory = "jmsListenerContainerQueue")
    public void receiveRedPackRecord(Integer mqRecordId) {
        receiveMsgRecord(mqRecordId, (args) -> {
            MqRecord mqRecord = (MqRecord) args[0];
            RedPackRecordDAO redPackRecordDAO = DAOFactory.getDAOByUrt(RedPackRecordDAO.class);
            RedPackRecord record = redPackRecordDAO.get(mqRecord.getRecordId().intValue());
            if (record == null || record.getStatus() == StatusConstant.STATUS_COMPLETE) {
                mqRecord.setStatus(record == null ? StatusConstant.STATUS_FAILED : StatusConstant.STATUS_COMPLETE);
                logger.error("can not get redPackRecord[{}] or it has been resolved for mqRecord[{}]", record == null ? null : record.getId(), mqRecord.getId());
            } else {
                try {
                    if (redPackManageService.handleSendRedPackSuccessMsg(record)) {
                        mqRecord.setStatus(StatusConstant.STATUS_COMPLETE);
                    } else {
                        logger.error("can not handle redPackRecord[{}]", record.getId());
                    }
                } catch (Exception e) {
                    logger.error("handle redPackRecord[{}] error cause:", record.getId(), e);
                }
            }
        });
    }

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
