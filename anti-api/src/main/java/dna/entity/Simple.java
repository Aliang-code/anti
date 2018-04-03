package dna.entity;


import dna.origins.commons.SerializableEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Aliang on 2017/7/27.
 * 实体类范例，务必实现SerializableEntity接口,注解使用的包不要导错
 */
@Table(name = "db_simple")
@Entity
public class Simple extends SerializableEntity {
    private Integer id;
    private String name;
    private Date createTime;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "name", nullable = false, length = 20)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "createTime", columnDefinition = "DATETIME")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
