package dna.origins.util.exception;

/**
 * Created by Aliang on 2017/7/23.
 */

public class CodedBaseRuntimeException extends RuntimeException implements CodedBase {
    private static final long serialVersionUID = -466214696181211521L;
    protected int code = 500;

    public CodedBaseRuntimeException() {
    }

    public CodedBaseRuntimeException(int code) {
        this.code = code;
    }

    public CodedBaseRuntimeException(int code, Throwable t) {
        super(t);
        this.code = code;
    }

    public CodedBaseRuntimeException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public CodedBaseRuntimeException(int code, String msg, Throwable t) {
        super(msg, t);
        this.code = code;
    }

    public CodedBaseRuntimeException(Throwable t) {
        super(t);
    }

    public CodedBaseRuntimeException(String msg) {
        super(msg);
    }

    public CodedBaseRuntimeException(String msg, Throwable t) {
        super(msg, t);
    }

    public int getCode() {
        return this.code;
    }

    public void throwThis() throws Exception {
        throw this;
    }
}
