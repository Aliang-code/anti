package dna.origins.commons;

public enum MessageType {
    SC_QUERY(0, "防伪查询消息"),
    BILL_UPLOAD(1, "单据上传消息"),
    WX_AUTH_CALLBACK(2, "微信授权事件消息"),
    WX_AUTH_UPDATE(3, "微信授权详情获取消息"),
    WX_AUTH_SUCCESS(4, "微信授权成功消息"),
    SEND_RED_PACK_SUCCESS(5, "微信授权成功消息");


    private int id;
    private String comment;

    MessageType(int id, String comment) {
        this.id = id;
        this.comment = comment;
    }

    public static MessageType getType(int id) {
        for (MessageType type : values()) {
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
