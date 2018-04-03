package dna.origins.annotation;

import java.lang.annotation.*;

/**
 * Created by Aliang on 2017/7/28.
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {

}
