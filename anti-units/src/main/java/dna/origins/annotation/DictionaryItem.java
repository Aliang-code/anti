package dna.origins.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictionaryItem {
    @AliasFor("path")
    String value() default "";

    @AliasFor("value")
    String path() default "";

    boolean isKey() default false;

    boolean isText() default false;
}
