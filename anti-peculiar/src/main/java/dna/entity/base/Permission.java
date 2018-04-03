package dna.entity.base;

import dna.origins.commons.IPermission;
import dna.origins.commons.SerializableEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "base_permission")
@Entity
public class Permission extends SerializableEntity implements IPermission {
    private static final long serialVersionUID = -7750725608959055904L;
    private Integer id;
    private String name;
    private Integer parentId;
    private String description;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    private List<Permission> childPms;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "name", nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "parentId")
    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Transient
    public List<Permission> getChildPms() {
        return childPms;
    }

    public void setChildPms(List<Permission> childPms) {
        this.childPms = childPms;
    }

//    public void addChildPm(Permission childPm) {
//        if (this.childPms == null) {
//            this.childPms = new ArrayList<>();
//        }
//        childPms.add(childPm);
//    }
}
