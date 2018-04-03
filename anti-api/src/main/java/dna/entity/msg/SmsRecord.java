package dna.entity.msg;

import dna.constants.StatusConstant;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "msg_smsRecord")
public class SmsRecord {
    private Integer id;
    private String companyCode;
    private Integer type;
    private String mobile;
    private String content;
    private String outId;
    private String bizId;
    private String errMsg;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    public SmsRecord() {
    }

    public SmsRecord(String companyCode, Integer type, String mobile, String content, String outId) {
        this.companyCode = companyCode;
        this.type = type;
        this.mobile = mobile;
        this.content = content;
        this.outId = outId;
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

    @Column(name = "mobile", nullable = false)
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Column(name = "content", nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(name = "outId")
    public String getOutId() {
        return outId;
    }

    public void setOutId(String outId) {
        this.outId = outId;
    }

    @Column(name = "bizId", unique = true)
    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    @Column(name = "errMsg")
    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
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
