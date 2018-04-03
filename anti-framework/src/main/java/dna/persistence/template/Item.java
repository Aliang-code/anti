package dna.persistence.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Item implements Serializable {
    private static final long serialVersionUID = 1298500836577889968L;
    private String parent;
    private String key;
    private String value;
    private transient List<Item> child;

    public Item(String parent, String key, String value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    public Item( String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Item> getChild() {
        return child;
    }

    public void addChild(Item item) {
        if(this.child==null){
            this.child=new ArrayList<>();
        }
        this.child.add(item);
    }
}
