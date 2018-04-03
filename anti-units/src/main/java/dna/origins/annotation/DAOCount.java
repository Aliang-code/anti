package dna.origins.annotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DAOCount {
    String name() default "";

    String sql() default "";

    QueryType queryType() default QueryType.HQL;
}
