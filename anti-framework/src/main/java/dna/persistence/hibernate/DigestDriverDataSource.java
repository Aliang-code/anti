package dna.persistence.hibernate;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.AtomikosSQLException;
import dna.origins.util.encrypt.CipherScaler;

public class DigestDriverDataSource extends AtomikosDataSourceBean {
    private final String PASSWORD = "password";
    private boolean digest = false;

    public boolean isDigest() {
        return digest;
    }

    public void setDigest(boolean digest) {
        this.digest = digest;
    }

    public synchronized void init() throws AtomikosSQLException {
        if (digest) {
            super.getXaProperties().setProperty(PASSWORD, CipherScaler.decode(this.getXaProperties().getProperty(PASSWORD)));
            digest = false;
        }
        super.init();
    }

    public static DigestDriverDataSource generateAtomikosDataSourceBean() throws Exception {
        DigestDriverDataSource atomikosDataSourceBean = new DigestDriverDataSource();
        atomikosDataSourceBean.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        atomikosDataSourceBean.setMinPoolSize(10);
        atomikosDataSourceBean.setMaxPoolSize(100);
        atomikosDataSourceBean.setBorrowConnectionTimeout(180);
        //atomikosDataSourceBean.setTestQuery("select 1");
        atomikosDataSourceBean.setLoginTimeout(180);
        atomikosDataSourceBean.setMaxLifetime(600);
        atomikosDataSourceBean.setMaxIdleTime(60);
        atomikosDataSourceBean.setReapTimeout(1000000);
        return atomikosDataSourceBean;
    }

}
