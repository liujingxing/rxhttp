package com.rxhttp.compiler;

import com.rxhttp.compiler.exception.ProcessingException;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import rxhttp.wrapper.annotation.Converter;
import rxhttp.wrapper.annotation.DefaultDomain;
import rxhttp.wrapper.annotation.Domain;
import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.annotation.Parser;

/**
 * User: ljx
 * Date: 2019/3/21
 * Time: 20:36
 */
public class AnnotationProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private boolean processed;

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
        annotations.add(Converter.class.getCanonicalName());
        annotations.add(Domain.class.getCanonicalName());
        annotations.add(DefaultDomain.class.getCanonicalName());
        annotations.add(Override.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        messager.printMessage(Kind.WARNING, "process start annotations" + annotations + " this=" + this);
        if (annotations.isEmpty() || processed) return true;
        try {
            RxHttpGenerator rxHttpGenerator = new RxHttpGenerator();

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

            ConverterAnnotatedClass converterAnnotatedClass = new ConverterAnnotatedClass();
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Converter.class)) {
                VariableElement variableElement = (VariableElement) annotatedElement;
                checkConverterValidClass(variableElement);
                converterAnnotatedClass.add(variableElement);
            }

            DomainAnnotatedClass domainAnnotatedClass = new DomainAnnotatedClass();
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Domain.class)) {
                VariableElement variableElement = (VariableElement) annotatedElement;
                checkVariableValidClass(variableElement);
                domainAnnotatedClass.add(variableElement);
            }

            Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(DefaultDomain.class);
            if (elementSet.size() > 1)
                throw new ProcessingException(elementSet.iterator().next(), "@DefaultDomain annotations can only be used once");
            else if (elementSet.iterator().hasNext()) {
                VariableElement variableElement = (VariableElement) elementSet.iterator().next();
                checkVariableValidClass(variableElement);
                rxHttpGenerator.setAnnotatedClass(variableElement);
            }
            rxHttpGenerator.setAnnotatedClass(paramsAnnotatedClass);
            rxHttpGenerator.setAnnotatedClass(parserAnnotatedClass);
            rxHttpGenerator.setAnnotatedClass(converterAnnotatedClass);
            rxHttpGenerator.setAnnotatedClass(domainAnnotatedClass);

            // Generate code
            rxHttpGenerator.generateCode(elementUtils, filer);
            processed = true;
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
                if (!typeMirror.toString().equals("rxhttp.wrapper.param.Param<P>")) continue;
                return;
            }
            TypeMirror superClassType = currentClass.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE) {
                throw new ProcessingException(element,
                    "The class %s annotated with @%s must inherit from %s",
                    element.getQualifiedName().toString(), Param.class.getSimpleName(),
                    "rxhttp.wrapper.param.Param");
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
                if (!typeMirror.toString().equals("rxhttp.wrapper.parse.Parser<T>")) continue;
                break All;
            }
            TypeMirror superClassType = currentClass.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE) {
                throw new ProcessingException(element,
                    "The class %s annotated with @%s must inherit from %s",
                    element.getQualifiedName().toString(), Parser.class.getSimpleName(),
                    "rxhttp.wrapper.parse.Parser<T>");
            }
            currentClass = (TypeElement) typeUtils.asElement(superClassType);
        }

//        for (Element enclosedElement : element.getEnclosedElements()) {
//            if (!(enclosedElement instanceof ExecutableElement)) continue;
//            if (!enclosedElement.getModifiers().contains(Modifier.PUBLIC)
//                    || !enclosedElement.getModifiers().contains(Modifier.STATIC)) continue;
//            if (!enclosedElement.toString().equals("<T>get(java.lang.Class<T>)")) continue;
//            ExecutableElement executableElement = (ExecutableElement) enclosedElement;
//            TypeMirror returnType = executableElement.getReturnType();
//            if (!typeUtils.asElement(returnType).toString()
//                    .equals(element.getQualifiedName().toString())) continue;
//            if (returnType instanceof DeclaredType) {
//                DeclaredType declaredType = (DeclaredType) returnType;
//                if (declaredType.getTypeArguments().size() == 1) return;
//            }
//        }

        // No empty constructor found
//        throw new ProcessingException(element,
//                "The class %s must provide an public static <T> %s get(Class<T> t) mehod",
//                element.getQualifiedName().toString(), element.getQualifiedName().toString() + "<T>");
    }

    private void checkConverterValidClass(VariableElement element) throws ProcessingException {
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
        if (!"rxhttp.wrapper.callback.IConverter".equals(element.asType().toString())) {
            throw new ProcessingException(element,
                "The variable %s is not a IConverter",
                element.getSimpleName().toString());
        }

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
