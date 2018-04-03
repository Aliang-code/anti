package dna.origins.commons;

import java.io.Serializable;

/**
 * entity超类，用于存储每一个entity实例的数据库来源
 */
public abstract class SerializableEntity implements Serializable {
    private String cid;

    public SerializableEntity() {
    }

    public SerializableEntity(String cid) {
        this.cid = cid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }
}
