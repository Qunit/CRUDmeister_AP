package nl.qunit.crudmeister.model;

import nl.qunit.crudmeister.annotations.CRUD;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
public class DocumentMapper {
    public <T> T map(Object document, Class<T> clazz) {
        CRUD crudTarget = clazz.getAnnotation(CRUD.class);
        CRUD crudSource = document.getClass().getAnnotation(CRUD.class);

        try {
            while (crudTarget.version() > crudSource.version()) {
                Class<? extends Migrator> migratorClass = crudSource.up();
                Migrator migrator = migratorClass.getConstructor().newInstance();
                document = migrator.migrate(document);
                crudSource = document.getClass().getAnnotation(CRUD.class);
            }

            while (crudTarget.version() < crudSource.version()) {
                Class<? extends Migrator> migratorClass = crudSource.down();
                Migrator migrator = migratorClass.getConstructor().newInstance();
                document = migrator.migrate(document);
                crudSource = document.getClass().getAnnotation(CRUD.class);
            }
            return clazz.cast(document);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
