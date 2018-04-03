package dna.persistence.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DataDictionary {
    private static final Logger logger = LoggerFactory.getLogger(DataDictionary.class);
    private LinkedHashMap<String, Item> itemMap;

    public DataDictionary(LinkedHashMap<String, Item> itemMap) {
        this.itemMap = itemMap;
    }

    public int size() {
        return itemMap.size();
    }

    public boolean isEmpty() {
        return itemMap.isEmpty();
    }

    public List<String> keys() {
        List<String> keys = new ArrayList<>();
        keys.addAll(itemMap.keySet());
        return keys;
    }

    public List<Item> values() {
        List<Item> values = new ArrayList<>();
        values.addAll(itemMap.values());
        return values;
    }

    public Item get(String key) {
        return itemMap.get(key);
    }

}
