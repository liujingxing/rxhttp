package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.rxhttpClassName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeVariableName
import rxhttp.wrapper.annotation.Converter
import java.util.*
import javax.annotation.processing.Messager
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Types

class ConverterVisitor(
    private val types: Types,
    private val logger: Messager
) {

    private val elementMap = LinkedHashMap<String, VariableElement>()

    fun add(element: VariableElement) {
        try {
            element.checkConverterValidClass(types)
            val annotation = element.getAnnotation(Converter::class.java)
            var name = annotation.name
            if (name.isBlank()) {
                name = element.simpleName.toString().firstLetterUpperCase()
            }
            if (elementMap.containsKey(name)) {
                val msg =
                    "The variable '${element.simpleName}' in the @Converter annotation 'name = $name' is duplicated"
                throw NoSuchElementException(msg)
            }
            elementMap[name] = element
        } catch (e: NoSuchElementException) {
            logger.error(e.message, element)
        }
    }

    fun getMethodList(): List<MethodSpec> {
        val typeVariableR = TypeVariableName.get("R", rxhttpClassName)     //泛型R
        return elementMap.mapNotNull { entry ->
            val key = entry.key
            val variableElement = entry.value
            val className = ClassName.get(variableElement.enclosingElement.asType())
            MethodSpec.methodBuilder("set$key")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return setConverter(\$T.${variableElement.simpleName})", className)
                .returns(typeVariableR)
                .build()
        }
    }
}

@Throws(NoSuchElementException::class)
private fun VariableElement.checkConverterValidClass(types: Types) {
    val variableName = simpleName.toString()

    val className = "rxhttp.wrapper.callback.IConverter"
    val typeElement = types.asElement(asType()) as? TypeElement
    if (!typeElement.instanceOf(className, types)) {
        throw NoSuchElementException("The variable '$variableName' must be IConverter")
    }

    var curParent = enclosingElement
    while (curParent is TypeElement) {
        if (!curParent.modifiers.contains(Modifier.PUBLIC)) {
            val msg = "The class '${curParent.qualifiedName}' must be public"
            throw NoSuchElementException(msg)
        }
        curParent = curParent.enclosingElement
    }

    if (!modifiers.contains(Modifier.PUBLIC)) {
        val msg =
            "The variable '$variableName' must be public, please add @JvmField annotation if you use kotlin"
        throw NoSuchElementException(msg)
    }
    if (!modifiers.contains(Modifier.STATIC)) {
        throw NoSuchElementException("The variable '$variableName' must be static")
    }
}

