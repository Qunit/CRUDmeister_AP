package nl.qunit.crudmeister.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class ControllerFacade {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DocumentMapper documentMapper;


    public <T> ResponseEntity<List<T>> getAll(Class<T> clazz) {
        List<Object> documents = mongoTemplate.findAll(Object.class, "testresources");
        List<T> docs = documents.stream()
                .map(document -> documentMapper.map(document, clazz))
                .toList();
        return ResponseEntity.ok().body(docs);
    }

    public <T> ResponseEntity<T> post(T toPost) {
        mongoTemplate.insert(toPost);

        return ResponseEntity.created(URI.create("testresource/")).body(toPost);
    }

    public <T> ResponseEntity<T> put(T toPost) {
        mongoTemplate.save(toPost);

        return ResponseEntity.ok().body(toPost);
    }

    public <T> ResponseEntity<T> getById(String id, Class<T> clazz) {
        Object document = mongoTemplate.findById(id, Object.class, "testresources");
        if (document == null) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
        }
        T doc = documentMapper.map(document, clazz);
        return ResponseEntity.ok().body(doc);
    }
}
