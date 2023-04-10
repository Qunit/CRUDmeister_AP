package nl.qunit.crudmeister.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public abstract class CRUDBase implements Serializable {
    @Id
    private String crudId;

    public String getCrudId() {
        return crudId;
    }

    public void setCrudId(String crudId) {
        this.crudId = crudId;
    }
}
