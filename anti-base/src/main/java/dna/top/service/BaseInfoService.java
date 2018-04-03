package dna.top.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dna.config.ThirdApiConfig;
import dna.constants.AreaConstant;
import dna.entity.top.Area;
import dna.origins.annotation.RpcService;
import dna.origins.commons.ResObject;
import dna.persistence.factory.DAOFactory;
import dna.persistence.regulate.HttpClientProcessor;
import dna.top.dao.AreaDAO;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 基本信息service层
 */
public class BaseInfoService {
    private static final Logger logger = LoggerFactory.getLogger(BaseInfoService.class);

    @Autowired
    private ThirdApiConfig thirdApiConfig;

    @Autowired
    private HttpClientProcessor httpClientProcessor;

    /**
     * 根据地区代码查询下属地区列表
     *
     * @param parentId 上级地区编号
     * @return List<Area>
     */
    @RpcService
    public List<Area> queryArea(String parentId) {
        AreaDAO areaDAO = DAOFactory.getDAO(AreaDAO.class);
        return areaDAO.findAreaByParentId(parentId);
    }

    /**
     * 根据地区代码查询地区完整名称
     *
     * @param regionId：地区代码
     * @return Address
     */
    @RpcService
    public String queryAddress(String regionId) {
        if (StringUtils.isBlank(regionId)) {
            return "暂无";
        }
        String address = "";
        AreaDAO areaDAO = DAOFactory.getDAO(AreaDAO.class);
        for (Area area = areaDAO.get(regionId); area != null; area = areaDAO.get(area.getParentId())) {
            address = area.getRegionName().concat(address);
            if ("0".equals(area.getParentId())) {
                break;
            }
        }
        return address;
    }

    /**
     * 从高德地图api更新本地数据库行政区划信息
     * <a herf='http://lbs.amap.com/api/webservice/guide/api/district'>API doc</a>
     *
     * @return
     */
    @RpcService
    @RequiresPermissions("topAdmin")
    public boolean updateAddressFromAMap() {
        try {
            String url = thirdApiConfig.getAmap_district_url();
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("key", thirdApiConfig.getAmap_ak()));
            params.add(new BasicNameValuePair("keywords", "100000"));//全国
            params.add(new BasicNameValuePair("subdistrict", "3"));//精确到区
            String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
            url = url.endsWith("?") ? url.concat(paramsStr) : url.concat("?".concat(paramsStr));
            ResObject<JSONObject> resObject = httpClientProcessor.getSimpleRequest(url, new ParameterizedTypeReference<JSONObject>() {
            });
            if (resObject.isSuccess()) {
                JSONObject jsonObject = resObject.getBody();
                if ("1".equals(jsonObject.get("status"))) {
                    JSONObject nationInfo = jsonObject.getJSONArray("districts").getJSONObject(0);
                    if ("100000".equals(nationInfo.getString("adcode"))) {
                        List<Area> areas = parseDistrictInfo(nationInfo.getJSONArray("districts"));
                        AreaDAO areaDAO = DAOFactory.getDAO(AreaDAO.class);
                        areaDAO.updateOrSaveAreas(areas);
                        logger.info("updateAddressFromAMap complete");
                        return true;
                    } else {
                        logger.error("updateAddressFromAMap error: districts info must begin with nationInfo");

                    }
                }
                logger.error("updateAddressFromAMap failed:{}", jsonObject.toString());
            }
        } catch (IOException e) {
            logger.error("updateAddressFromAMap error:", e);
        }
        return false;
    }

    private List<Area> parseDistrictInfo(JSONArray districts) {
        List<Area> areas = new ArrayList<>();
        districts.toJavaList(JSONObject.class).forEach(d -> {
            String adcode = d.getString("adcode");
            String name = d.getString("name");
            String parentId;
            switch (d.getString("level")) {
                case AreaConstant.AREA_LEVEL_PROVINCE:
                    parentId = "0";
                    break;
                case AreaConstant.AREA_LEVEL_CITY:
                    parentId = adcode.replaceFirst("....$", "0000");
                    break;
                case AreaConstant.AREA_LEVEL_DISTRICT:
                    parentId = adcode.replaceFirst("..$", "00");
                    break;
                default:
                    return;
            }
            Area area = new Area(adcode, name, parentId);
            areas.add(area);
            areas.addAll(parseDistrictInfo(d.getJSONArray("districts")));
        });
        return areas;
    }
}
