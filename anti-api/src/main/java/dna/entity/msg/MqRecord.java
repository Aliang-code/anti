package dna.entity.msg;

import dna.constants.StatusConstant;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "msg_mqRecord")
public class MqRecord implements Serializable {
    private static final long serialVersionUID = -7548890107743446674L;
    private Integer id;
    private String companyCode;
    private Integer type;
    private Long recordId;
    private String extraArg1;
    private String extraArg2;
    private String extraArgs;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    public MqRecord() {
    }

    public MqRecord(String companyCode, Integer type, Long recordId, String extraArg1, String extraArg2, String extraArgs) {
        this.companyCode = companyCode;
        this.type = type;
        this.recordId = recordId;
        this.extraArg1 = extraArg1;
        this.extraArg2 = extraArg2;
        this.extraArgs = extraArgs;
        this.status = StatusConstant.STATUS_WAIT;
        this.createTime = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "companyCode", nullable = false)
    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    @Column(name = "type", nullable = false)
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Column(name = "recordId")
    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @Column(name = "extraArg1", length = 50)
    public String getExtraArg1() {
        return extraArg1;
    }

    public void setExtraArg1(String extraArg1) {
        this.extraArg1 = extraArg1;
    }

    @Column(name = "extraArg2", length = 50)
    public String getExtraArg2() {
        return extraArg2;
    }

    public void setExtraArg2(String extraArg2) {
        this.extraArg2 = extraArg2;
    }

    @Column(name = "extraArgs", length = 500)
    public String getExtraArgs() {
        return extraArgs;
    }

    public void setExtraArgs(String extraArgs) {
        this.extraArgs = extraArgs;
    }

    @Column(name = "status", nullable = false)
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "createTime", nullable = false)
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Column(name = "updateTime")
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
