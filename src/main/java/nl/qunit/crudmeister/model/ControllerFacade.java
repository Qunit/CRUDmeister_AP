package nl.qunit.crudmeister.model;


import nl.qunit.crudmeister.annotations.CRUD;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class ControllerFacade {
    public static final String LAST_SEEN_ID = "CRUDmeister-LastSeenId";
    public static final int DEFAULT_PAGE_SIZE = 100;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    MongoConverter mongoConverter;

    public <T extends CRUDBase> ResponseEntity<List<T>> getAll(Class<T> clazz, String startFromId, int pageSize) {
        if(pageSize<= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        Query query;
        if (startFromId == null || startFromId.isEmpty()) {
            query = new Query().with(Sort.by(Sort.Direction.ASC, "_id")).limit(pageSize);
        } else {
            query = new Query(Criteria.where("_id").gt(new ObjectId(startFromId))).with(Sort.by(Sort.Direction.ASC, "_id")).limit(pageSize);
        }

        List<CRUDBase> documents = mongoTemplate.find(query, CRUDBase.class, clazz.getAnnotation(CRUD.class).collection());
        String lastSeenId = documents.size() > 0 ? documents.get(documents.size() - 1).getCrudId().toString() : "";
        List<T> docs = documents.stream()
                .map(document -> documentMapper.map(document, clazz))
                .toList();
        return ResponseEntity.ok()
                .header(LAST_SEEN_ID, lastSeenId)
                .body(docs);
    }

    public <T extends CRUDBase> ResponseEntity<T> post(T toPost) {
        mongoTemplate.insert(toPost);

        return ResponseEntity.created(URI.create(toPost.getClass().getAnnotation(CRUD.class).collection() + "/" + toPost.getCrudId())).body(toPost);
    }

    public <T extends CRUDBase> ResponseEntity<T> put(T toPost) {
        mongoTemplate.save(toPost);

        return ResponseEntity.ok().body(toPost);
    }

    public <T extends CRUDBase> ResponseEntity<T> getById(String id, Class<T> clazz) {
        CRUDBase document = mongoTemplate.findById(id, CRUDBase.class, clazz.getAnnotation(CRUD.class).collection());
        if (document == null) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
        }
        T doc = documentMapper.map(document, clazz);
        return ResponseEntity.ok().body(doc);
    }
}
