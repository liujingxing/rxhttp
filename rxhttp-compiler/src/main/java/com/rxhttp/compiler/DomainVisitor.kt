package com.rxhttp.compiler

import com.rxhttp.compiler.exception.ProcessingException
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Domain
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class DomainVisitor {

    private val elementMap = LinkedHashMap<String, VariableElement>()

    fun add(element: VariableElement) {
        checkVariableValidClass(element)
        val annotation = element.getAnnotation(Domain::class.java)
        var name: String = annotation.name
        if (name.isEmpty()) {
            name = element.simpleName.toString()
        }
        elementMap[name] = element
    }

    //对url添加域名方法
    fun getMethodList(): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        for ((key, value) in elementMap) {
            methodList.add(
                MethodSpec.methodBuilder("setDomainTo${key}IfAbsent")
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(
                        """return setDomainIfAbsent(${"$"}T.${value.simpleName});""",
                        ClassName.get(value.enclosingElement.asType()),
                    )
                    .returns(r).build()
            )
        }
        return methodList
    }
}

@Throws(ProcessingException::class)
fun checkVariableValidClass(element: VariableElement) {
    if (!element.modifiers.contains(Modifier.PUBLIC)) {
        throw ProcessingException(
            element,
            "The variable ${element.simpleName} is not public, please add @JvmField annotation if you use kotlin"
        )
    }
    if (!element.modifiers.contains(Modifier.STATIC)) {
        throw ProcessingException(
            element,
            "The variable ${element.simpleName} is not static"
        )
    }
}