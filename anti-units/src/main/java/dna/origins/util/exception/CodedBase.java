package dna.origins.util.exception;

/**
 * Created by Aliang on 2017/7/23.
 */
public interface CodedBase {
    int getCode();

    String getMessage();

    Throwable getCause();

    StackTraceElement[] getStackTrace();

    void throwThis() throws Exception;
}
