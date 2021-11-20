package com.rxhttp.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Domain
import java.util.*
import javax.annotation.processing.Messager
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class DomainVisitor(private val logger: Messager) {

    private val elementMap = LinkedHashMap<String, VariableElement>()

    fun add(element: VariableElement) {
        try {
            element.checkVariableValidClass()
            val annotation = element.getAnnotation(Domain::class.java)
            var name: String = annotation.name
            if (name.isBlank()) {
                name = element.simpleName.toString().firstLetterUpperCase()
            }
            elementMap[name] = element
        } catch (e: NoSuchElementException) {
            logger.error(e.message, element)
        }
    }

    //对url添加域名方法
    fun getMethodList(): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        for ((key, value) in elementMap) {
            MethodSpec.methodBuilder("setDomainTo${key}IfAbsent")
                .addModifiers(Modifier.PUBLIC)
                .addCode(
                    """return setDomainIfAbsent(${"$"}T.${value.simpleName});""",
                    ClassName.get(value.enclosingElement.asType()),
                )
                .returns(r)
                .build()
                .apply { methodList.add(this) }
        }
        return methodList
    }
}

@Throws(NoSuchElementException::class)
fun VariableElement.checkVariableValidClass() {
    if (!modifiers.contains(Modifier.PUBLIC)) {
        val msg =
            "The variable '$simpleName' must be public, please add @JvmField annotation if you use kotlin"
        throw NoSuchElementException(msg)
    }
    if (!modifiers.contains(Modifier.STATIC)) {
        throw NoSuchElementException("The variable '$simpleName' is not static")
    }
}