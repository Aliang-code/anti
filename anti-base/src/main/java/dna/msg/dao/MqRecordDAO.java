package dna.msg.dao;

import dna.entity.msg.MqRecord;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.SingleSourceDAOMethod;

import java.util.List;

public abstract class MqRecordDAO extends SingleSourceDAOMethod<MqRecord> {
    public MqRecordDAO() {
        super();
        this.setEntity(MqRecord.class);
    }

    @DAOMethod(sql = "from MqRecord where status=:status")
    public abstract List<MqRecord> findByStatus(@DAOParam("status") int status);

}
