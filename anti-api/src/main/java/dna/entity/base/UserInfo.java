package dna.entity.base;

import dna.origins.annotation.DictionaryItem;
import dna.origins.commons.SerializableEntity;

import java.util.Date;
import java.util.List;

/**
 * Created by zhangxw on 2017/8/15.
 * 用户信息管理实体类
 */

public class UserInfo extends SerializableEntity {
    private static final long serialVersionUID = -6205215418425765460L;
    private Integer id;         //用户ID
    private String userName;    //用户名
    private String realName;     //真名
    @DictionaryItem("base/dictionary/UserType.dic")
    private Integer userType;
    private Integer targetId;
    private Integer status;     //状态
    private Date createTime;
    private Date updateTime;
    private List<UserRole> userRoles;

    public UserInfo() {
    }

    public UserInfo(User user) {
        this.id=user.getId();
        this.userName=user.getUserName();
        this.realName = user.getRealName();
        this.userType = user.getUserType();
        this.targetId = user.getTargetId();
        this.status=user.getStatus();
        this.createTime = user.getCreateTime();
        this.updateTime = user.getUpdateTime();
    }

    public UserInfo(User user, List<UserRole> userRoles) {
        this.id = user.getId();
        this.userName = user.getUserName();
        this.realName = user.getRealName();
        this.userType = user.getUserType();
        this.targetId = user.getTargetId();
        this.status = user.getStatus();
        this.createTime = user.getCreateTime();
        this.updateTime = user.getUpdateTime();
        this.userRoles = userRoles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
