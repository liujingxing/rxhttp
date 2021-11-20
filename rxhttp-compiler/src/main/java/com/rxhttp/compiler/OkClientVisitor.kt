package com.rxhttp.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.OkClient
import java.util.*
import javax.annotation.processing.Messager
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Types

class OkClientVisitor(private val logger: Messager) {

    private val elementMap = LinkedHashMap<String, VariableElement>()

    fun add(element: VariableElement, types: Types) {
        try {
            element.checkOkClientValidClass(types)
            val annotation = element.getAnnotation(OkClient::class.java)
            var name = annotation.name
            if (name.isBlank()) {
                name = element.simpleName.toString().firstLetterUpperCase()
            }
            elementMap[name] = element
        } catch (e: NoSuchElementException) {
            logger.error(e.message, element)
        }
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

@Throws(NoSuchElementException::class)
private fun VariableElement.checkOkClientValidClass(types: Types) {
    if (!modifiers.contains(Modifier.PUBLIC)) {
        val msg =
            "The variable '$simpleName' must be public, please add @JvmField annotation if you use kotlin"
        throw NoSuchElementException(msg)
    }
    if (!modifiers.contains(Modifier.STATIC)) {
        throw NoSuchElementException("The variable '$simpleName' is not static")
    }

    val className = "okhttp3.OkHttpClient"
    val typeElement = types.asElement(asType()) as? TypeElement
    if (!typeElement.instanceOf(className, types)) {
        throw NoSuchElementException("The variable '$simpleName' is not a OkHttpClient")
    }
}