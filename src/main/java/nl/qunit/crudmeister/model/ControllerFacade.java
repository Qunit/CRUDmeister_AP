package nl.qunit.crudmeister.model;


import com.mongodb.client.MongoCollection;
import nl.qunit.crudmeister.annotations.CRUD;
import nl.qunit.crudmeister.annotations.CRUDCollection;
import org.bson.Document;
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
import java.time.LocalDateTime;
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
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        Query query;
        if (startFromId == null || startFromId.isEmpty()) {
            query = new Query().with(Sort.by(Sort.Direction.ASC, "_id")).limit(pageSize);
        } else {
            query = new Query(Criteria.where("_id").gt(new ObjectId(startFromId))).with(Sort.by(Sort.Direction.ASC, "_id")).limit(pageSize);
        }

        CRUDCollection crudCollection = getCrudCollection(clazz);

        List<CRUDBase> documents = mongoTemplate.find(query, CRUDBase.class, crudCollection.collection());

        String lastSeenId = documents.size() > 0 ? documents.get(documents.size() - 1).getCrudId().toString() : "";
        List<T> docs = documents.stream()
                .map(document -> documentMapper.map(document, clazz))
                .toList();
        return ResponseEntity.ok()
                .header(LAST_SEEN_ID, lastSeenId)
                .body(docs);
    }

    private static <T extends CRUDBase> CRUDCollection getCrudCollection(Class<T> clazz) {
        Class<?> collectionClazz = clazz.getAnnotation(CRUD.class).collection();
        CRUDCollection crudCollection = collectionClazz.getAnnotation(CRUDCollection.class);
        return crudCollection;
    }

    public <T extends CRUDBase> ResponseEntity<T> post(T toPost) {
        T inserted = mongoTemplate.insert(toPost);
        CRUDCollection crudCollection = storeInHistoryCollection(inserted);
        return ResponseEntity.created(URI.create(crudCollection.collection() + "/" + toPost.getCrudId())).body(toPost);
    }

    public <T extends CRUDBase> ResponseEntity<T> put(T toSave) {
        T saved = mongoTemplate.save(toSave);
        storeInHistoryCollection(saved);
        return ResponseEntity.ok().body(saved);
    }

    private <T extends CRUDBase> CRUDCollection storeInHistoryCollection(T saved) {
        CRUDCollection crudCollection = getCrudCollection(saved.getClass());
        String collectionName = crudCollection.collection();
        if (crudCollection.keepHistory()) {

            String historyCollectionName = collectionName + "_history";

            long count = mongoTemplate.count(new Query(), historyCollectionName);
            if (count == 0) {
                MongoCollection<Document> historyCollection = mongoTemplate.createCollection(historyCollectionName);
                Document index = new Document("crudId", 1);
                index.put("created", 1);
                historyCollection.createIndex(index);
            }

            CRUDHistory history = CRUDHistory.builder()
                    .archivedItem(saved)
                    .created(LocalDateTime.now())
                    .crudId(saved.getCrudId())
                    .createdBy(getAuthenticatedUser())
                    .build();
            mongoTemplate.insert(history, historyCollectionName);
        }
        return crudCollection;
    }


    public <T extends CRUDBase> ResponseEntity<T> getById(String id, Class<T> clazz) {
        CRUDCollection collection = getCrudCollection(clazz);
        CRUDBase document = mongoTemplate.findById(id, CRUDBase.class, collection.collection());
        if (document == null) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
        }
        T doc = documentMapper.map(document, clazz);
        return ResponseEntity.ok().body(doc);
    }

    private String getAuthenticatedUser() {
        return "anonymous";
    }
}
