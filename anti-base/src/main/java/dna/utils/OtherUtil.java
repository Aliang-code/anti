package dna.utils;

import dna.config.ThirdApiConfig;
import dna.origins.commons.ServiceException;
import dna.origins.util.encrypt.HashEncryptUtils;
import dna.persistence.factory.DAOFactory;
import dna.top.dao.EnterpriseInfoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OtherUtil {
    @Autowired
    private ThirdApiConfig thirdApiConfig;

    public void verifyPublicRequest(String cid, String timestamp, String sign, String... args) {
        String values = "";
        for (String s : args) {
            values = values.concat(s);
        }
        EnterpriseInfoDAO enterpriseInfoDAO = DAOFactory.getDAO(EnterpriseInfoDAO.class);
        if (!sign.equals(HashEncryptUtils.MD5(values + cid + timestamp + thirdApiConfig.getAnti_platform_app_secret()))) {
            throw new ServiceException("签名无效！");
        } else if (enterpriseInfoDAO.getByCode(cid) == null) {
            throw new ServiceException("企业正在维护，请稍后重试！");
        }

    }

    public String generatePublicSign(String cid, String timestamp, String... args) {
        String values = "";
        for (String s : args) {
            values = values.concat(s);
        }
        return HashEncryptUtils.MD5(values + cid + timestamp + thirdApiConfig.getAnti_platform_app_secret());
    }
}
