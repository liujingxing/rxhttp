package com.rxhttp.compiler

import com.rxhttp.compiler.exception.ProcessingException
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Converter
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Types

class ConverterVisitor {

    private val elementMap = LinkedHashMap<String, VariableElement>()

    fun add(element: VariableElement, types: Types) {
        checkConverterValidClass(element, types)
        val annotation = element.getAnnotation(Converter::class.java)
        var name = annotation.name
        if (name.isEmpty()) {
            name = element.simpleName.toString()
        }
        elementMap[name] = element
    }

    fun getMethodList(): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        for ((key, value) in elementMap) {
            MethodSpec.methodBuilder("set$key")
                .addModifiers(Modifier.PUBLIC)
                .addStatement(
                    "return setConverter(\$T.${value.simpleName})",
                    ClassName.get(value.enclosingElement.asType()),
                )
                .returns(r)
                .build()
                .apply { methodList.add(this) }
        }
        return methodList
    }
}

@Throws(ProcessingException::class)
private fun checkConverterValidClass(element: VariableElement, types: Types) {
    if (!element.modifiers.contains(Modifier.PUBLIC)) {
        throw ProcessingException(
            element,
            "The variable ${element.simpleName} is not public"
        )
    }
    if (!element.modifiers.contains(Modifier.STATIC)) {
        throw ProcessingException(
            element,
            "The variable ${element.simpleName} is not static"
        )
    }
    val className = "rxhttp.wrapper.callback.IConverter"
    val typeElement = types.asElement(element.asType()) as TypeElement
    if (!typeElement.instanceOf(className, types)) {
        throw ProcessingException(
            element,
            "The variable ${element.simpleName} is not a IConverter"
        )
    }
}

