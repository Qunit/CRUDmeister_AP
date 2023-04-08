package nl.qunit.crudmeister;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public enum ControllerMethod {
    GET_ALL("getAll", RequestMethod.GET, GetMapping.class),
    GET_BY_ID("getById", RequestMethod.GET, GetMapping.class),
    POST("post", RequestMethod.POST, PostMapping.class),
    PUT("put", RequestMethod.PUT, PutMapping.class);

    private final String methodName;
    private final RequestMethod requestMethod;
    private Class<?> mappingClass;

    ControllerMethod(String methodName, RequestMethod requestMethod, Class<?> mappingClass) {
        this.methodName = methodName;
        this.requestMethod = requestMethod;
        this.mappingClass = mappingClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public Class<?> getMappingClass() {
        return mappingClass;
    }
}
