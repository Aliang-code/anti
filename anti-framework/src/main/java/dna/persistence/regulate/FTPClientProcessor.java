package dna.persistence.regulate;

import dna.origins.commons.ServiceException;
import dna.origins.util.client.FTPTransfer;
import dna.persistence.factory.FTPClientFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;


public class FTPClientProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FTPClientProcessor.class);
    private GenericObjectPool<FTPClient> pool;
    private static final int DEFAULT_INITIAL_SIZE = 3;
    private int initialSize;

    public FTPClientProcessor(FTPClientFactory factory, GenericObjectPoolConfig poolConfig) {
        this(factory, poolConfig, DEFAULT_INITIAL_SIZE);
    }

    public FTPClientProcessor(FTPClientFactory factory, GenericObjectPoolConfig poolConfig, int initialSize) {
        pool = new GenericObjectPool<>(factory, poolConfig);
        this.initialSize = initialSize;
    }

    /**
     * 初始化连接池
     */
    public void init() {
        for (int i = 0; i < initialSize; i++) {
            try {
                pool.addObject();
            } catch (Exception e) {
                logger.error("loading FtpClient error...", e);
            }
        }
    }

    public boolean uploadFile(String path, String filename, InputStream input) {
        FTPClient ftp = getFTPClient();
        boolean result = FTPTransfer.uploadFile(ftp, path, filename, input);
        returnFTPClient(ftp);
        return result;
    }

    public boolean downloadFile(String remotePath, String fileName, OutputStream outputStream) {
        FTPClient ftp = getFTPClient();
        boolean result = FTPTransfer.downloadFile(ftp, remotePath, fileName, outputStream);
        returnFTPClient(ftp);
        return result;
    }

    public FTPClient getFTPClient() {
        FTPClient ftpClient;
        try {
            ftpClient = pool.borrowObject();
        } catch (Exception e) {
            logger.error("borrowObject error:", e);
            ftpClient = null;
        }
        if (ftpClient == null) {
            throw new ServiceException(ServiceException.SERVICE_ERROR, "文件服务器繁忙，请稍候重试！");
        } else {
            return ftpClient;
        }
    }

    public void returnFTPClient(FTPClient ftpClient) {
        pool.returnObject(ftpClient);
    }

    public void close() {
        if (pool != null) {
            pool.close();
            logger.info("ftpClientPool has been closed");
        }
    }
}