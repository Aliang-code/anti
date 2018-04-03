package dna.origins.commons;

public enum DatabaseType {
    MYSQL(1, "org.hibernate.dialect.MySQL5Dialect"),
    SQL_SERVER(2, "org.hibernate.dialect.SQLServer2008Dialect");

    private int id;
    private String dialect;

    DatabaseType(int id, String dialect) {
        this.id = id;
        this.dialect = dialect;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
