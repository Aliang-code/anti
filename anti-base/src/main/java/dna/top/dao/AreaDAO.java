package dna.top.dao;

import dna.entity.top.Area;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.factory.AppContextHolder;
import dna.persistence.hibernate.AbstractDAOMethod;
import dna.persistence.regulate.DictionaryService;

import java.util.ArrayList;
import java.util.List;


public abstract class AreaDAO extends AbstractDAOMethod<Area> {

    public AreaDAO() {
        super("");
        this.setEntity(Area.class);
    }

    @DAOMethod(sql = "from Area where parentId=:parentId")
    public abstract List<Area> findAreaByParentId(@DAOParam("parentId") String parentId);

    @DAOMethod(sql = "select regionName from Area where regionId=:regionId")
    public abstract String getAddress(@DAOParam("regionId") String regionId);

    @DAOMethod(sql = "select regionId from Area")
    public abstract List<String> findRegionId();

    public void updateOrSaveAreas(List<Area> areas) {
        List<String> keys = AppContextHolder.getBean("dictionaryService", DictionaryService.class).getDataDictionary("top/dictionary/Address.dic").keys();
        List<Area> updateAreas=new ArrayList<>();
        List<Area> saveAreas=new ArrayList<>();
        areas.forEach(area -> {
            if (keys.contains(area.getRegionId())) {
                updateAreas.add(area);
            } else {
                saveAreas.add(area);
            }
        });
        updateBatch(updateAreas);
        saveBatch(saveAreas);
    }
}
