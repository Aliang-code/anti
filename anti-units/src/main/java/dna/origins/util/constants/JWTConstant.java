package dna.origins.util.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aliang on 2017/8/10.
 */
public interface JWTConstant {
    String TOKEN_HEAD_NAME = "X-Authorization";
    String UUID_HEAD_NAME = "X-UUID";
    String TOKEN_COOIKE_NAME = "HZWJ_TK";
    String UUID_COOIKE_NAME = "HZWJ_UUID";
    int COOKIE_MAXAGE = 7 * 24 * 60 * 60;//秒


    List<String> LOCALHOST_IP = new ArrayList<String>() {{
        add("127.0.0.1");
        add("0:0:0:0:0:0:0:1");
    }};
}
