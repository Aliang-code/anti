package dna.top.dao;

import dna.constants.StatusConstant;
import dna.entity.top.CodeMakeRule;
import dna.origins.annotation.DAOCount;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.Date;
import java.util.List;

public abstract class CodeMakeRuleDAO extends AbstractDAOMethod<CodeMakeRule> {
    public CodeMakeRuleDAO() {
        super("");
        this.setEntity(CodeMakeRule.class);
    }

    public Integer save(CodeMakeRule rule) {
        rule.setCreateTime(rule.getCreateTime() == null ? new Date() : rule.getCreateTime());
        rule.setStatus(StatusConstant.STATUS_ENABLE);
        return super.save(rule);
    }

    @DAOCount( name = "countCodeMakeRuleByCompanyCode")
    @DAOMethod(sql = "from CodeMakeRule where companyCode = :companyCode")
    public abstract List<CodeMakeRule> findCodeMakeRuleByCompanyCode(@DAOParam("companyCode") String companyCode, @DAOParam(start = true) Integer start, @DAOParam(limit = true) Integer limit);
}
