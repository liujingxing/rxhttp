package com.rxhttp.compiler

import com.rxhttp.compiler.exception.ProcessingException
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Converter
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
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
    var classType = element.asType()
    val iConverter = "rxhttp.wrapper.callback.IConverter"
    while (true) {
        if (iConverter == classType.toString()) return
        //TypeMirror转TypeElement
        val currentClass = types.asElement(classType) as TypeElement
        //遍历实现的接口有没有IConverter接口
        if (currentClass.findIConverter(types, iConverter)) return
        //未遍历到IConverter，则找到父类继续，一直循环下去，直到最顶层的父类
        classType = currentClass.superclass
        if (classType.kind == TypeKind.NONE) {
            throw ProcessingException(
                element,
                "The variable ${element.simpleName} is not a IConverter"
            )
        }
    }
}

fun TypeElement.findIConverter(types: Types, iConverter: String): Boolean {
    for (mirror in interfaces) {
        return if (mirror.toString() == "rxhttp.wrapper.callback.IConverter") {
            true
        } else {
            (types.asElement(mirror) as? TypeElement)?.findIConverter(types, iConverter)
                ?: return false
        }
    }
    return false
}
