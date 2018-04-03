package dna.constants;

public enum DocType {
    COMMON(1, "common"), RECORD(2, "record"), DATA(3, "data");
    private int id;
    private String name;

    DocType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static String getName(int id) {
        for (DocType d : DocType.values()) {
            if (d.getId() == id) {
                return d.getName();
            }
        }
        return COMMON.getName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
