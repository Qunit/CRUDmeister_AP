package nl.qunit.crudmeister.annotations;

import nl.qunit.crudmeister.model.Migrator;
import nl.qunit.crudmeister.model.NoOpMigrator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface CRUDCollection {

    String collection();

    boolean keepHistory() default false;

}
