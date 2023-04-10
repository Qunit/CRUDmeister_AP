package nl.qunit.crudmeister.model;

public class NoOpMigrator<SOURCETARGET extends CRUDBase> implements Migrator<SOURCETARGET, SOURCETARGET> {
    @Override
    public SOURCETARGET doMigrate(SOURCETARGET source) {
        return source;
    }
}
