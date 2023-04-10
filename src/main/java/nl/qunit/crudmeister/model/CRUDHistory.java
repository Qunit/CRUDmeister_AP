package nl.qunit.crudmeister.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CRUDHistory<T extends CRUDBase> implements Serializable {
    @Id
    private String id;

    private LocalDateTime created;

    private String createdBy;

    @Indexed
    private String crudId;

    public T archivedItem;
}
