package dna.msg.dao;

import dna.entity.msg.SmsRecord;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.SingleSourceDAOMethod;

public abstract class SmsRecordDAO extends SingleSourceDAOMethod<SmsRecord> {
    public SmsRecordDAO() {
        super();
        this.setEntity(SmsRecord.class);
    }

    @DAOMethod(sql = "from SmsRecord where bizId=:bizId")
    public abstract SmsRecord getByBizId(@DAOParam("bizId") String bizId);
}
