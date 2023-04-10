package nl.qunit.crudmeister.model;

public interface Migrator<SOURCE extends CRUDBase, TARGET extends CRUDBase> {

    default TARGET migrate(SOURCE source) {
        TARGET target = doMigrate(source);
        target.setCrudId(source.getCrudId());
        return target;
    };

    TARGET doMigrate(SOURCE document);
}
