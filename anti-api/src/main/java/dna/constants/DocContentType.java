package dna.constants;

public enum DocContentType {
    UNKNOWN("unknown", "application/octet-stream"), TXT("txt", "application/octet-stream"), HTML("html", "text/html"),
    JPE("jpe", "image/jpeg"), JPEG("jpeg", "image/jpeg"), GIF("gif", "image/gif"), PNG("png", "image/png"), BMP("bmp", "application/x-bmp"),
    MP3("mp3", "audio/mp3"), MP4("mp4", "video/mpeg4"), WAV("wav", "audio/wav"),
    PDF("pdf", "application/pdf"), XML("xml", "application/xml"), DOC("doc", "application/msword"), DOCX("docx", "application/msword");
    private String suffix;
    private String type;

    DocContentType(String suffix, String type) {
        this.suffix = suffix;
        this.type = type;
    }

    public static String getType(String suffix) {
        for (DocContentType d : DocContentType.values()) {
            if (d.getSuffix().equalsIgnoreCase(suffix)) {
                return d.getType();
            }
        }
        return null;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
