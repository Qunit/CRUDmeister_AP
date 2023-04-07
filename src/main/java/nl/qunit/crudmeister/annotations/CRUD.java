package nl.qunit.crudmeister.annotations;

import nl.qunit.crudmeister.model.Migrator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
@Document(collection = "#{#CRUD.collection()}")
public @interface CRUD {
    int version();

    Class<? extends Migrator> down();

    Class<? extends Migrator> up();

    String collection();

    String contentName();

    Annotations[] resourceAnnotations() default {};

    Annotations[] controllerAnnotations() default {};
}
