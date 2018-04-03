package dna.entity.base;

import dna.origins.annotation.DictionaryItem;
import dna.origins.commons.SerializableEntity;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "base_user")
public class User extends SerializableEntity {
    private static final long serialVersionUID = -5678258329587151876L;
    private Integer id;
    private String userName;
    private String password;
    private String salt;
    @DictionaryItem("base/dictionary/UserType.dic")
    private Integer userType;
    private Integer targetId;
    private String realName;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    public User(){}

    public User(User user) {
        this.id=user.getId();
        this.userName=user.getUserName();
        this.realName = user.getRealName();
        this.status=user.getStatus();
        this.createTime = user.getCreateTime();
        this.updateTime = user.getUpdateTime();
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

    @Column(name = "userName", nullable = false, unique = true)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "password", nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "salt",nullable = false)
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Column(name = "realName", nullable = false)
    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    @Column(name = "userType", nullable = false)
    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    @Column(name = "targetId")
    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targerId) {
        this.targetId = targerId;
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

    public boolean valid() {
        return StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(realName) && StringUtils.isNotBlank(password) && userType != null;
    }

}
