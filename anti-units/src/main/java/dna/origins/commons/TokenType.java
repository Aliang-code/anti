package dna.origins.commons;

public enum TokenType {
    NONE(0), PLATFORM_TOKEN(1), AUTHORIZATION_TOKEN(2);
    private int type;

    TokenType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
