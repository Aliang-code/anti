package dna.origins.annotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DAOMethod {
    String sql() default "";

    int limit() default 100;

    int start() default 0;
}
