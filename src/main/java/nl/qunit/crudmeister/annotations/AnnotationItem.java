package nl.qunit.crudmeister.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnnotationItem {
    Class<? extends Annotation> type();

    ValidationParameter[] parameters() default {};
}
