package dna.origins.util.constants;

public interface PlatformConstant {
    String PLATFORM_CODE_WJZM = "WJZM";
    String ANTI_APPID = "antiappid";

    String CALLBACK_TYPE_TICKET = "component_verify_ticket";
    String CALLBACK_TYPE_AUTHORIZED = "authorized";
    String CALLBACK_TYPE_UNAUTHORIZED = "unauthorized";
    String CALLBACK_TYPE_UPDATEAUTHORIZED = "updateauthorized";

    String WX_PARAM_TIMESTAMP = "timestamp";
    String WX_PARAM_NONCE = "nonce";
    String WX_PARAM_MSG_SIGNATURE = "msg_signature";
    String WX_PARAM_ENCRYPT_TYPE = "encrypt_type";
    String WX_PARAM_CALLBACK_DATA = "postData";

    String WX_PARAM_AUTH_CODE = "auth_code";
    String WX_PARAM_EXPIRES_IN = "expires_in";

    String WX_PARAM_OPENID = "openid";

    String WX_PAY_PARAM_MCH_ID = "mch_id";
    String WX_PAY_PARAM_SUB_MCH_ID = "sub_mch_id";
    String WX_PAY_PARAM_WISHING = "wishing";
    String WX_PAY_PARAM_SENDNAME = "send_name";
    String WX_PAY_PARAM_RE_OPENID = "re_openid";
    String WX_PAY_PARAM_WXAPPID = "wxappid";
    String WX_PAY_PARAM_MSGAPPID = "msgappid";
    String WX_PAY_PARAM_MCH_BILLNO = "mch_billno";
    String WX_PAY_PARAM_ACT_NAME = "act_name";
    String WX_PAY_PARAM_REMARK = "remark";
    String WX_PAY_PARAM_TOTAL_AMOUNT = "total_amount";

    String WX_PAY_SEND_LIST_ID = "send_listid";

    String PARAM_RESPONSE_DATA = "resData";
    String PARAM_PLATFORM_ID = "platformId";
    String PARAM_PLATFORM_CODE = "platformCode";
    String PARAM_APPID = "appid";
    String PARAM_NOTE = "note";
    String PARAM_MQRECORD_ID = "mqRecordId";
    String PARAM_STATE = "state";
    String PARAM_REDIRECT_URL = "redirectUrl";
    String PARAM_AUTH_CODE = "authCode";
    String PARAM_USER_AUTH_TYPE = "userAuthType";

    String PARAM_ERR_CODE = "errcode";
    String PARAM_ERR_MSG = "errmsg";

    String AUTH_TYPE_ACCOUNT = "1";
    String AUTH_TYPE_PROGRAM = "2";
    String AUTH_TYPE_DEFAULT = "3";

    String AUTH_SCOPE_BASE = "snsapi_base";
    String AUTH_SCOPE_USERINFO = "snsapi_userinfo";
}
