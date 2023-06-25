package com.rxhttp.compiler.kapt

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind

fun List<ExecutableElement>.findTypeArgumentConstructorFun(typeParametersSize: Int): ExecutableElement? {
    for (constructor in this) {
        if (!constructor.modifiers.contains(Modifier.PUBLIC)) continue
        constructor.parameters.forEach { variableElement ->
            if (variableElement.asType().toString() == "java.lang.reflect.Type[]")
                return constructor
        }
        //构造方法参数个数小于泛型个数，则遍历下一个
        if (constructor.parameters.size < typeParametersSize) continue

        //如果解析器有n个泛型，则构造方法前n个参数，必须是Type类型
        val match = constructor.parameters.subList(0, typeParametersSize).all {
            "java.lang.reflect.Type" == it.asType().toString()
        }
        if (match) return constructor
    }
    return null
}

//获取public构造方法
fun TypeElement.getPublicConstructors() =
    getVisibleConstructorFun().filter {
        it.modifiers.contains(Modifier.PUBLIC)
    }

//获取 public、protected 构造方法
fun TypeElement.getVisibleConstructorFun(): List<ExecutableElement> {
    val funList = ArrayList<ExecutableElement>()
    enclosedElements.forEach {
        if (it is ExecutableElement &&
            it.kind == ElementKind.CONSTRUCTOR &&
            (Modifier.PUBLIC in it.getModifiers() || Modifier.PROTECTED in it.getModifiers())
        ) {
            funList.add(it)
        }
    }
    return funList
}

internal fun TypeElement?.instanceOf(className: String, types: Types): Boolean {
    if (this == null) return false
    if (className == qualifiedName.toString()) return true
    superTypes().forEach {
        val typeElement = types.asElement(it) as? TypeElement
        if (typeElement.instanceOf(className, types)) return true
    }
    return false
}

internal fun TypeElement.superTypes() =
    interfaces.toMutableList().apply {
        if (superclass.kind != TypeKind.NONE)
            add(0, superclass)
    }

internal fun Messager.error(msg: CharSequence?, e: Element) {
    printMessage(Kind.ERROR, msg ?: "", e)
}

internal fun String.firstLetterUpperCase(): String {
    val charArray = toCharArray()
    val firstChar = charArray.firstOrNull() ?: return this
    if (firstChar.code in 97..122) {
        charArray[0] = firstChar.minus(32)
    }
    return String(charArray)
}

fun com.squareup.javapoet.ClassName.parameterizedBy(
    vararg typeArguments: com.squareup.javapoet.TypeName
): com.squareup.javapoet.ParameterizedTypeName =
    com.squareup.javapoet.ParameterizedTypeName.get(this, *typeArguments)