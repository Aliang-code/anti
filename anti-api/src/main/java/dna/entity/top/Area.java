package dna.entity.top;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 区域信息实体类
 */
@Entity
@Table(name = "top_area")
public class Area implements Serializable {

    private static final long serialVersionUID = 8550560157548056370L;
    private String regionId;//行政区划代码
    private String regionName;//地区名称
    private String parentId;//上级代码

    public Area() {
    }

    public Area(String regionId, String regionName, String parentId) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.parentId = parentId;
    }

    @Id
    @GeneratedValue(generator = "adCode")
    @GenericGenerator(name = "adCode", strategy = "assigned")
    @Column(name = "regionId", nullable = false, unique = true)
    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionCode) {
        this.regionId = regionCode;
    }

    @Column(name = "regionName", nullable = false)
    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    @Column(name = "parentId", nullable = false)
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

}
