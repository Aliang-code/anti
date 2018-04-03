package dna.persistence.factory;

import dna.persistence.template.FTPClientConfig;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPClientFactory implements PooledObjectFactory<FTPClient> {

    private static Logger logger = LoggerFactory.getLogger(FTPClientFactory.class);

    private FTPClientConfig config;

    public FTPClientFactory(FTPClientConfig config) {
        this.config = config;
    }

    @Override
    public PooledObject<FTPClient> makeObject() throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(config.getClientTimeout());
        try {
            ftpClient.connect(config.getHost(), config.getPort());
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.warn("FTPServer refused connection");
                return null;
            }
            boolean result = ftpClient.login(config.getUsername(), config.getPassword());
            if (!result) {
                logger.warn("ftpClient login failed... username is {}", config.getUsername());
            }
            ftpClient.setFileType(config.getTransferFileType());
            ftpClient.setBufferSize(config.getBufferSize());
            ftpClient.setControlEncoding(config.getEncoding());
            if (config.isPassiveMode()) {
                ftpClient.enterLocalPassiveMode();
            }
        } catch (Exception e) {
            logger.error("create ftp connection failed...{}", e.getMessage());
            throw e;
        }

        return new DefaultPooledObject<>(ftpClient);
    }

    @Override
    public void destroyObject(PooledObject<FTPClient> pooledObject) throws Exception {
        FTPClient ftpClient = pooledObject.getObject();
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (Exception e) {
            logger.error("ftp client logout failed...{}", e.getMessage());
            throw e;
        } finally {
            if (ftpClient != null) {
                ftpClient.disconnect();
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<FTPClient> pooledObject) {
        FTPClient ftpClient = pooledObject.getObject();
        try {
            return ftpClient.sendNoOp();
        } catch (Exception e) {
            logger.error("Failed to validate client: {}", e);
        }
        return false;
    }

    @Override
    public void activateObject(PooledObject<FTPClient> pooledObject) throws Exception {
        //Do nothing
    }

    @Override
    public void passivateObject(PooledObject<FTPClient> pooledObject) throws Exception {
        //Do nothing
    }
}
