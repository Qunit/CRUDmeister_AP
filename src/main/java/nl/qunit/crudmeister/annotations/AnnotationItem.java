package nl.qunit.crudmeister.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnnotationItem {
    Class<?> type();

    ValidationParameter[] parameters() default {};
}
