package dna.config;

import dna.base.dao.PermissionDAO;
import dna.base.dao.RoleDAO;
import dna.msg.service.SmsProcessor;
import dna.persistence.factory.DAOFactory;
import dna.persistence.shiro.ApplicationToken;
import dna.persistence.shiro.UserRoleToken;
import dna.top.service.BaseInfoService;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class InitializationConfig implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(InitializationConfig.class);
    @Autowired
    private BaseInfoService baseInfoService;
    @Autowired
    private SmsProcessor smsProcessor;

    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("-------------------基础数据更新开始-------------------");
        ApplicationToken.roleName = new HashSet<>(DAOFactory.getDAOByCid(RoleDAO.class, UserRoleToken.CID_BASE).findAllRoleNames());
        ApplicationToken.permissionNames = new HashSet<>(DAOFactory.getDAOByCid(PermissionDAO.class, UserRoleToken.CID_BASE).findAllPermissionNames());
        ApplicationToken token = new ApplicationToken(UserRoleToken.CID_BASE);
        SecurityUtils.getSubject().login(token);
        baseInfoService.updateAddressFromAMap();
        smsProcessor.startMessageListener();
        logger.info("-------------------基础数据更新结束-------------------");
    }
}
