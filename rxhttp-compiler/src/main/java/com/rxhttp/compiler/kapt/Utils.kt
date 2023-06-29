package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.J_ARRAY_TYPE
import com.rxhttp.compiler.J_TYPE
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind


fun List<ParameterSpec>.toParamNames(): String {
    val paramNames = StringBuilder()
    forEachIndexed { index, parameterSpec ->
        if (index > 0) paramNames.append(", ")
        paramNames.append(parameterSpec.name)
    }
    return paramNames.toString()
}


//判断解析器构造方法是否有效
fun ExecutableElement.isValid(typeCount: Int): Boolean {
    //1、非public方法，无效
    if (!modifiers.contains(Modifier.PUBLIC)) return false
    val parameters = parameters
    if (parameters.isEmpty()) {
        //2、构造方法没有参数，且泛型数量等于0，有效，反之无效
        return typeCount == 0
    }
    val firstParameter = parameters.first()
    val firstParameterType = TypeName.get(firstParameter.asType())
    if (firstParameterType == J_ARRAY_TYPE) {
        //3、第一个参数为Type类型数组 或 Type类型可变参数, 有效
        return true
    }
    //4、构造方法参数数量小于泛型数量，无效
    if (parameters.size < typeCount) return false
    //5、构造方法前n个参数，皆为Type类型，有效  n为泛型数量
    return parameters.subList(0, typeCount).all { J_TYPE == TypeName.get(it.asType()) }
}

//获取onParser方法返回类型
fun TypeElement.findOnParserFunReturnType(): TypeName? {
    val function = enclosedElements.find {
        it is ExecutableElement   //是方法
                && it.getModifiers().contains(Modifier.PUBLIC)  //public修饰
                && !it.getModifiers().contains(Modifier.STATIC) //非静态
                && it.simpleName.toString() == "onParse"  //onParse方法
                && it.parameters.size == 1  //只有一个参数
                && TypeName.get(it.parameters[0].asType())
            .toString() == "okhttp3.Response"  //参数是okhttp3.Response类型
    } ?: return null
    return TypeName.get((function as ExecutableElement).returnType)
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

fun TypeElement.getConstructors(): List<ExecutableElement> {
    return enclosedElements.mapNotNull {
        if (it is ExecutableElement &&
            it.kind == ElementKind.CONSTRUCTOR &&
            (Modifier.PUBLIC in it.getModifiers() || Modifier.PROTECTED in it.getModifiers())
        ) {
            it
        } else null
    }
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

fun ClassName.parameterizedBy(vararg typeArguments: TypeName): ParameterizedTypeName =
    ParameterizedTypeName.get(this, *typeArguments)