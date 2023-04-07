package nl.qunit.crudmeister.model;

public interface Migrator<SOURCE, TARGET> {
    TARGET migrate(SOURCE document);
}
