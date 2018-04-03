package dna.origins.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DAOParam {
    String value() default "";

    boolean limit() default false;

    boolean start() default false;

    LikeType like() default LikeType.NONE;
}
