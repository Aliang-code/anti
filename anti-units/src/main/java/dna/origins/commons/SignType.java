package dna.origins.commons;

public enum SignType {
    ANTI(0),
    WX_PAY(1);

    private int id;

    SignType(int id) {
        this.id = id;
    }

    public static SignType getType(int id) {
        for (SignType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
