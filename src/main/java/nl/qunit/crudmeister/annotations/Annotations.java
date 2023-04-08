package nl.qunit.crudmeister.annotations;

import nl.qunit.crudmeister.ControllerMethod;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Annotations {
    String fieldName() default "";

    AnnotationItem[] annotations() default {};

    ControllerMethod[] methods();
}
