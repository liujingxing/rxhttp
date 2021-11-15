package com.rxhttp.compiler

import com.rxhttp.compiler.exception.ProcessingException
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.OkClient
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class OkClientVisitor {

    private val elementMap = LinkedHashMap<String, VariableElement>()

    fun add(element: VariableElement) {
        checkOkClientValidClass(element)
        val annotation = element.getAnnotation(OkClient::class.java)
        var name = annotation.name
        if (name.isEmpty()) {
            name = element.simpleName.toString()
        }
        elementMap[name] = element
    }

    fun getMethodList(): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        for ((key, value) in elementMap) {
            val className = ClassName.get(value.enclosingElement.asType())
            MethodSpec.methodBuilder("set$key")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return setOkClient(\$T.${value.simpleName})", className)
                .returns(r)
                .build()
                .apply { methodList.add(this) }
        }
        return methodList
    }
}

@Throws(ProcessingException::class)
private fun checkOkClientValidClass(element: VariableElement) {
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
    val classType = element.asType()
    if ("okhttp3.OkHttpClient" != classType.toString()) {
        throw ProcessingException(
            element,
            "The variable ${element.simpleName} is not a OkHttpClient"
        )
    }
}