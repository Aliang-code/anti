package dna.origins.commons;

import dna.origins.util.exception.CodedBaseRuntimeException;

/**
 * Created by Aliang on 2017/7/23.
 */
public class ServiceException extends CodedBaseRuntimeException {
    private static final long serialVersionUID = -8481811634176212223L;
    public static int ENTITIY_NOT_FOUND = 404;
    public static int SESSION_ACQUIRE_FALIED = 501;
    public static int SCHEMA_NOT_FOUND = 504;
    public static int VALUE_NEEDED = 505;
    public static int EVAL_FALIED = 506;
    public static int VALIDATE_FALIED = 507;
    public static int DATABASE_ACCESS_FAILED = 510;
    public static int ACCESS_DENIED = 511;
    public static int DAO_NOT_FOUND = 512;
    public static int SERVICE_ERROR = 609;

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(Throwable e) {
        super(e);
    }

    public ServiceException(int code, Throwable e) {
        super(code, e);
    }

    public ServiceException(Throwable e, int code, String msg) {
        super(code, msg, e);
    }

    public ServiceException(int code, String msg) {
        super(code, msg);
    }
}