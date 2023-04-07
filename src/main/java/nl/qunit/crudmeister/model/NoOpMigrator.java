package nl.qunit.crudmeister.model;

public class NoOpMigrator<SOURCETARGET> implements Migrator<SOURCETARGET, SOURCETARGET> {
    @Override
    public SOURCETARGET migrate(SOURCETARGET document) {
        return document;
    }
}
