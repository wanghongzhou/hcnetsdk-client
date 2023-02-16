package com.github.whz.client.basic.view;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Brian
 */
@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface FXMLView {

    String[] css() default {};

    String value() default "";

    String title() default "";

    String bundle() default "";

    String stageStyle() default "UTILITY";
}
