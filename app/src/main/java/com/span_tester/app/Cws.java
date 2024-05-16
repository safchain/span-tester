package com.span_tester.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cws {
    public String operationName() default "";

    public String fileField() default "";

    public String fileValue() default "";

    public String domainName() default "";
}
