package dna.top.dao;

import dna.constants.StatusConstant;
import dna.entity.top.CodePackRecord;
import dna.origins.annotation.DAOCount;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.Date;
import java.util.List;

public abstract class CodePackRecordDAO extends AbstractDAOMethod<CodePackRecord> {
    public CodePackRecordDAO() {
        super("");
        this.setEntity(CodePackRecord.class);
    }

    public Integer save(CodePackRecord record) {
        record.setCreateTime(record.getCreateTime() == null ? new Date() : record.getCreateTime());
        //设置初始状态为 0：未制码
        record.setStatus(StatusConstant.STATUS_DISABLE);
        return super.save(record);
    }

    @DAOCount(name = "countAllCodePackRecord")
    @DAOMethod(sql = "from CodePackRecord order by createTime desc")
    public abstract List<CodePackRecord> findAllCodePackRecord (@DAOParam(start = true) Integer start, @DAOParam(limit = true) Integer limit);

    @DAOMethod(sql = "select p.id from CodePackRecord p,CodeMakeRule r where p.companyCode=:companyCode and r.activateType=:activateType and r.status=1 and p.codeMakeRuleId=r.id")
    public abstract List<Integer> findIdByActivateTypeAndCompanyCode(@DAOParam("activateType")Integer activateType,@DAOParam("companyCode") String companyCode );
}
