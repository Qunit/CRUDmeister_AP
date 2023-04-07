package nl.qunit.crudmeister;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import nl.qunit.crudmeister.annotations.CRUD;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
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
                    .addField(FieldSpec.builder(ClassName.get("nl.qunit.crudmeister", "ControllerFacade"), "controllerFacade")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("new $T()", ClassName.get("nl.qunit.crudmeister", "ControllerFacade"))
                            .build())
                    .addMethod(generateGetAllMethod(typeElement, crudAnnotation));

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
                .returns(returnType);

        // Generate the method body
        methodBuilder.addStatement("return controllerFacade.getAll($T.class)", typeElement);

        return methodBuilder.build();
    }
}