package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.r
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Domain
import java.util.*
import javax.annotation.processing.Messager
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Types

class DomainVisitor(
    private val types: Types,
    private val logger: Messager
) {

    private val elementMap = LinkedHashMap<String, VariableElement>()

    fun add(element: VariableElement) {
        try {
            element.checkVariableValidClass(types)
            val annotation = element.getAnnotation(Domain::class.java)
            var name: String = annotation.name
            if (name.isBlank()) {
                name = element.simpleName.toString().firstLetterUpperCase()
            }
            if (elementMap.containsKey(name)) {
                val msg =
                    "The variable '${element.simpleName}' in the @Domain annotation 'name = $name' is duplicated"
                throw NoSuchElementException(msg)
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
fun VariableElement.checkVariableValidClass(types: Types) {
    val variableName = simpleName.toString()

    val typeElement = types.asElement(asType()) as? TypeElement
    val className = "java.lang.String"
    if (!typeElement.instanceOf(className, types)) {
        throw NoSuchElementException("The variable '$variableName' must be String")
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