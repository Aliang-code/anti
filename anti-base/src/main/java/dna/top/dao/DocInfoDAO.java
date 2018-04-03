package dna.top.dao;

import dna.entity.top.DocInfo;
import dna.persistence.hibernate.AbstractDAOMethod;

public abstract class DocInfoDAO extends AbstractDAOMethod<DocInfo> {

    public DocInfoDAO() {
        super("");
        this.setEntity(DocInfo.class);//设置实体类
    }

}
