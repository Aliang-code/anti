package dna.origins.util.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FTPTransfer {
    private static final Logger logger = LoggerFactory.getLogger(FTPTransfer.class);

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param ftp
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     * @Version1.0 2017-08-16 09:46:23 by Aliang
     */
    public static boolean uploadFile(FTPClient ftp, String path, String filename, InputStream input) {
        try {
            ftp.changeWorkingDirectory("/");
            String dirs[] = path.split("/|\\\\");
            for (String dir : dirs) {
                if (StringUtils.isNotBlank(dir)) {
                    ftp.makeDirectory(dir);
                    ftp.changeWorkingDirectory(dir);
                }
            }
            ftp.storeFile(filename, input);
            input.close();
            return true;
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            return false;
        }
    }

    /**
     * Description: 从FTP服务器下载文件
     *
     * @param ftp
     * @param remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @return
     * @Version. 2017-08-16 09:54:32 by Aliang
     */
    public static boolean downloadFile(FTPClient ftp, String remotePath, String fileName, OutputStream outputStream) {
        try {
            ftp.changeWorkingDirectory(remotePath);//转移到FTP服务器目录
            //FTPFile[] fs = ftp.listFiles();
            ftp.retrieveFile(fileName, outputStream);
            return true;
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }
    }
}
