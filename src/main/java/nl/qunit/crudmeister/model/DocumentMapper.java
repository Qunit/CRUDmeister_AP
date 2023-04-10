package nl.qunit.crudmeister.model;

import nl.qunit.crudmeister.annotations.CRUD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
public class DocumentMapper {
    @Autowired
    private ApplicationContext applicationContext;

    public <T extends CRUDBase> T map(CRUDBase document, Class<T> clazz) {
        CRUD crudTarget = clazz.getAnnotation(CRUD.class);
        CRUD crudSource = document.getClass().getAnnotation(CRUD.class);

        while (crudTarget.version() > crudSource.version()) {
            Class<? extends Migrator> migratorClass = crudSource.up();

            Migrator migrator = applicationContext.getBean(migratorClass);
            document = migrator.migrate(document);
            crudSource = document.getClass().getAnnotation(CRUD.class);
        }

        while (crudTarget.version() < crudSource.version()) {
            Class<? extends Migrator> migratorClass = crudSource.down();
            Migrator migrator = applicationContext.getBean(migratorClass);
            document = migrator.migrate(document);
            crudSource = document.getClass().getAnnotation(CRUD.class);
        }
        return clazz.cast(document);
    }

}
