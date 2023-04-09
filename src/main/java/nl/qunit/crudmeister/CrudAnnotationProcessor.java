package nl.qunit.crudmeister;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import jakarta.validation.Valid;
import nl.qunit.crudmeister.annotations.AnnotationItem;
import nl.qunit.crudmeister.annotations.CRUD;
import nl.qunit.crudmeister.groups.Post;
import nl.qunit.crudmeister.groups.Put;
import nl.qunit.crudmeister.model.ControllerFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// Declare the supported annotation types
@SupportedAnnotationTypes("nl.qunit.crudmeister.annotations.CRUD")
@SupportedSourceVersion(SourceVersion.RELEASE_18)
@AutoService(Processor.class)
public class CrudAnnotationProcessor extends AbstractProcessor {

    // Declare a reference to the Filer for generating code
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(CRUD.class)) {
            // Get the annotation and the class on which it is defined
            CRUD crudAnnotation = element.getAnnotation(CRUD.class);
            TypeElement typeElement = (TypeElement) element;

            // Create the class name for the controller
            String controllerClassName = typeElement.getSimpleName().toString() + "Controller";
            // Create the package name for the controller
            String packageName = typeElement.getEnclosingElement().toString();

            // Generate the code for the controller class
            TypeSpec.Builder controllerBuilder = TypeSpec.classBuilder(controllerClassName)
                    .addAnnotation(Controller.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(FieldSpec.builder(ControllerFacade.class, "controllerFacade")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("new $T()", ControllerFacade.class)
                            .build())
                    .addMethod(generateGetAllMethod(typeElement, crudAnnotation))
                    .addMethod(generateGetById(typeElement, crudAnnotation))
                    .addMethod(generatePostPut(typeElement, crudAnnotation, ControllerMethod.POST, Post.class))
                    .addMethod(generatePostPut(typeElement, crudAnnotation, ControllerMethod.PUT, Put.class));

            // Add the RequestMapping annotation to the class
            AnnotationSpec requestMappingAnnotation = AnnotationSpec.builder(RequestMapping.class)
                    .addMember("value", "$S", "/api")
                    .build();
            controllerBuilder.addAnnotation(requestMappingAnnotation);

            // Generate the code for the controller class file
            TypeSpec controllerClass = controllerBuilder.build();
            JavaFile javaFile = JavaFile.builder(packageName, controllerClass)
                    .build();

            try {
                // Write the generated code to a file
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private MethodSpec generatePostPut(TypeElement typeElement, CRUD crudAnnotation, ControllerMethod method, Class<?> validatorClass) {
        // Create the ResponseEntity<List<ClassName>> return type
        ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(ResponseEntity.class),
                ClassName.get(typeElement)

        );


        // Create the method signature and annotations
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getMethodName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(method.getMappingClass())
                        .addMember("value", "$S", "/testresources")
                        .addMember("consumes", "$S", "application/vnd." + crudAnnotation.contentName() + "+json;version=" + crudAnnotation.version())
                        .addMember("produces", "$S", "application/vnd." + crudAnnotation.contentName() + "+json;version=" + crudAnnotation.version())
                        .build())
                .addAnnotation(ResponseBody.class)
                .addAnnotations(getAnnotations(crudAnnotation, method))
                .addParameter(ParameterSpec.builder(ClassName.get(typeElement), "element")
                        .addAnnotation(AnnotationSpec.builder(Validated.class)
                                .addMember("value", "$T.class", validatorClass)
                                .build())
                        .addAnnotation(RequestBody.class)
                        .build())
                .returns(returnType);

        // Generate the method body
        methodBuilder.addStatement("return controllerFacade." + method.getMethodName() + "(element)", typeElement);

        return methodBuilder.build();

    }

    private MethodSpec generateGetAllMethod(TypeElement typeElement, CRUD crudAnnotation) {
        // Create the ResponseEntity<List<ClassName>> return type
        ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(ResponseEntity.class),
                ParameterizedTypeName.get(
                        ClassName.get(List.class),
                        ClassName.get(typeElement)
                )
        );

        // Create the method signature and annotations
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                        .addMember("value", "$S", "/testresources")
                        .addMember("produces", "$S", "application/vnd." + crudAnnotation.contentName() + "+json;version=" + crudAnnotation.version())
                        .build())
                .addAnnotation(ResponseBody.class)
                .addAnnotations(getAnnotations(crudAnnotation, ControllerMethod.GET_ALL))
                .returns(returnType);

        // Generate the method body
        methodBuilder.addStatement("return controllerFacade.getAll($T.class)", typeElement);

        return methodBuilder.build();
    }

    private Iterable<AnnotationSpec> getAnnotations(CRUD crudAnnotation, ControllerMethod controllerMethod) {
        return Arrays.stream(crudAnnotation.controllerAnnotations())
                .filter(annotation -> Arrays.asList(annotation.methods()).contains(controllerMethod))
                .flatMap(annotation -> Arrays.stream(annotation.annotations()))
                .map(this::mapAnnotationItem).toList();
    }

    private AnnotationSpec mapAnnotationItem(AnnotationItem annotationItem) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(getMirrorredType(annotationItem));
        Arrays.stream(annotationItem.parameters())
                .forEach(parameter -> builder.addMember(parameter.key(), parameter.value()));
        return builder.build();
    }

    private ClassName getMirrorredType(AnnotationItem annotationItem) {
        try {
            annotationItem.type();
        } catch (MirroredTypeException mte) {
            return ClassName.bestGuess(mte.getTypeMirror().toString());
        }
        throw new IllegalStateException("AnnotationItem.type() should have thrown a MirroredTypeException");
    }

    private MethodSpec generateGetById(TypeElement typeElement, CRUD crudAnnotation) {
        // Create the ResponseEntity<List<ClassName>> return type
        ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(ResponseEntity.class),
                ClassName.get(typeElement)

        );

        // Create the method signature and annotations
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                        .addMember("value", "$S", "/testresources/{id}")
                        .addMember("produces", "$S", "application/vnd." + crudAnnotation.contentName() + "+json;version=" + crudAnnotation.version())
                        .build())
                .addAnnotation(ResponseBody.class)
                .addAnnotations(getAnnotations(crudAnnotation, ControllerMethod.GET_BY_ID))
                .addParameter(ParameterSpec.builder(
                                ClassName.get(String.class), "id")
                        .addAnnotation(AnnotationSpec.builder(
                                PathVariable.class)
                                .addMember("value", "$S", "id")
                                .build())
                        .build())
                .returns(returnType);

        // Generate the method body
        methodBuilder.addStatement("return controllerFacade.getById(id, $T.class)", typeElement);

        return methodBuilder.build();
    }
}