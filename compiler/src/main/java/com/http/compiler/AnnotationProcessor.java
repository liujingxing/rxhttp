package com.http.compiler;

import com.google.auto.service.AutoService;
import httpsender.wrapper.annotation.Domain;
import httpsender.wrapper.annotation.Param;
import httpsender.wrapper.annotation.Parser;
import com.http.compiler.exception.ProcessingException;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * User: ljx
 * Date: 2019/3/21
 * Time: 20:36
 */
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    private Types    typeUtils;
    private Messager messager;
    private Filer    filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Param.class.getCanonicalName());
        annotations.add(Parser.class.getCanonicalName());
        annotations.add(Domain.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            ParamsGenerator paramsGenerator = new ParamsGenerator();

            ParamsAnnotatedClass paramsAnnotatedClass = new ParamsAnnotatedClass();
            for (Element element : roundEnv.getElementsAnnotatedWith(Param.class)) {
                TypeElement typeElement = (TypeElement) element;
                checkParamsValidClass(typeElement);
                paramsAnnotatedClass.add(typeElement);
            }

            ParserAnnotatedClass parserAnnotatedClass = new ParserAnnotatedClass();
            for (Element element : roundEnv.getElementsAnnotatedWith(Parser.class)) {
                TypeElement typeElement = (TypeElement) element;
                checkParserValidClass(typeElement);
                parserAnnotatedClass.add(typeElement);
            }

            DomainAnnotatedClass domainAnnotatedClass = new DomainAnnotatedClass();
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Domain.class)) {
                VariableElement variableElement = (VariableElement) annotatedElement;
                checkVariableValidClass(variableElement);
                domainAnnotatedClass.add(variableElement);
            }
            paramsGenerator.setParamsAnnotatedClass(paramsAnnotatedClass);
            paramsGenerator.setParserAnnotatedClass(parserAnnotatedClass);
            paramsGenerator.setDomainAnnotatedClass(domainAnnotatedClass);
            // Generate code
            paramsGenerator.generateCode(elementUtils, filer);
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void checkParamsValidClass(TypeElement element) throws ProcessingException {
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(element,
                    "The class %s is not public",
                    Param.class.getSimpleName());
        }
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(element,
                    "The class %s is abstract. You can't annotate abstract classes with @%",
                    element.getSimpleName().toString(), Param.class.getSimpleName());
        }

        TypeElement currentClass = element;
        while (true) {
            List<? extends TypeMirror> interfaces = currentClass.getInterfaces();
            for (TypeMirror typeMirror : interfaces) {
                if (!typeMirror.toString().equals("httpsender.wrapper.param.Param")) continue;
                return;
            }
            TypeMirror superClassType = currentClass.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE) {
                throw new ProcessingException(element,
                        "The class %s annotated with @%s must inherit from %s",
                        element.getQualifiedName().toString(), Param.class.getSimpleName(),
                        "httpsender.wrapper.param.Param");
            }
            currentClass = (TypeElement) typeUtils.asElement(superClassType);
        }
    }

    private void checkParserValidClass(TypeElement element) throws ProcessingException {
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(element,
                    "The class %s is not public",
                    Parser.class.getSimpleName());
        }
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(element,
                    "The class %s is abstract. You can't annotate abstract classes with @%",
                    element.getSimpleName().toString(), Parser.class.getSimpleName());
        }

        TypeElement currentClass = element;
        All:
        while (true) {
            List<? extends TypeMirror> interfaces = currentClass.getInterfaces();
            for (TypeMirror typeMirror : interfaces) {
                if (!typeMirror.toString().equals("httpsender.wrapper.parse.Parser<T>")) continue;
                break All;
            }
            TypeMirror superClassType = currentClass.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE) {
                throw new ProcessingException(element,
                        "The class %s annotated with @%s must inherit from %s",
                        element.getQualifiedName().toString(), Parser.class.getSimpleName(),
                        "httpsender.wrapper.parse.Parser<T>");
            }
            currentClass = (TypeElement) typeUtils.asElement(superClassType);
        }

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (!(enclosedElement instanceof ExecutableElement)) continue;
            if (!enclosedElement.getModifiers().contains(Modifier.PUBLIC)
                    || !enclosedElement.getModifiers().contains(Modifier.STATIC)) continue;
            if (!enclosedElement.toString().equals("<T>get(java.lang.Class<T>)")) continue;
            ExecutableElement executableElement = (ExecutableElement) enclosedElement;
            TypeMirror returnType = executableElement.getReturnType();
            if (!typeUtils.asElement(returnType).toString()
                    .equals(element.getQualifiedName().toString())) continue;
            if (returnType instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) returnType;
                if (declaredType.getTypeArguments().size() == 1) return;
            }
        }

        // No empty constructor found
        throw new ProcessingException(element,
                "The class %s must provide an public static <T> %s get(Class<T> t) mehod",
                element.getQualifiedName().toString(), element.getQualifiedName().toString() + "<T>");
    }

    private void checkVariableValidClass(VariableElement element) throws ProcessingException {
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(element,
                    "The variable %s is not public",
                    element.getSimpleName());
        }
        if (!element.getModifiers().contains(Modifier.STATIC)) {
            throw new ProcessingException(element,
                    "The variable %s is not static",
                    element.getSimpleName().toString());
        }
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
