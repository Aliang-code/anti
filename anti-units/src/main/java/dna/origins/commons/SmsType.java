package dna.origins.commons;

public enum SmsType {
    VERIFY_CODE(0, "SMS_126275330", "您的验证码${code}，该验证码5分钟内有效，请勿泄漏于他人！");


    private int id;
    private String tempCode;
    private String tempContent;

    SmsType(int id, String tempCode, String tempContent) {
        this.id = id;
        this.tempCode = tempCode;
        this.tempContent = tempContent;
    }

    public static SmsType getType(int id) {
        for (SmsType type : values()) {
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

    public String getTempCode() {
        return tempCode;
    }

    public void setTempCode(String tempCode) {
        this.tempCode = tempCode;
    }

    public String getTempContent() {
        return tempContent;
    }

    public void setTempContent(String tempContent) {
        this.tempContent = tempContent;
    }
}
