package com.rxhttp.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.javapoet.JClassName
import com.squareup.kotlinpoet.javapoet.JTypeName
import com.squareup.kotlinpoet.javapoet.JTypeVariableName
import com.squareup.kotlinpoet.javapoet.KTypeName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toJClassName
import com.squareup.kotlinpoet.javapoet.toJTypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import org.jetbrains.annotations.Nullable

/**
 * User: ljx
 * Date: 2021/11/11
 * Time: 17:38
 */
//获取对象类型，包名+类名
internal fun KSTypeReference.getQualifiedName() =
    resolve().declaration.qualifiedName?.asString()

//获取方法名
internal fun KSFunctionDeclaration.getFunName() = simpleName.asString()

@KotlinPoetKspPreview
@KotlinPoetJavaPoetPreview
internal fun KSClassDeclaration.toJClassName(): JTypeName {
    return toClassName().toJClassName()
}

internal typealias JModifier = javax.lang.model.element.Modifier
internal typealias JParameterSpec = com.squareup.javapoet.ParameterSpec

internal fun Modifier.toJModifier(): JModifier? {
    return when (this) {
        Modifier.PUBLIC -> JModifier.PUBLIC
        Modifier.PROTECTED -> JModifier.PROTECTED
        Modifier.PRIVATE -> JModifier.PRIVATE
        Modifier.ABSTRACT -> JModifier.ABSTRACT
        Modifier.JAVA_DEFAULT -> JModifier.DEFAULT
        Modifier.JAVA_STATIC -> JModifier.STATIC
        Modifier.FINAL -> JModifier.FINAL
        Modifier.JAVA_TRANSIENT -> JModifier.TRANSIENT
        Modifier.JAVA_VOLATILE -> JModifier.VOLATILE
        Modifier.JAVA_SYNCHRONIZED -> JModifier.SYNCHRONIZED
        Modifier.JAVA_NATIVE -> JModifier.NATIVE
        Modifier.JAVA_STRICT -> JModifier.STATIC
        else -> null
    }
}

//kotlinpoet 1.10.2版本 对于 kotlin.Unit、kotlin.CharSequence 类型，不会正确转换，所以需要手动处理
@KotlinPoetJavaPoetPreview
@KotlinPoetKspPreview
internal fun KSTypeReference.toJavaTypeName(
    typeParamResolver: TypeParameterResolver = TypeParameterResolver.EMPTY
) = toTypeName(typeParamResolver).toJavaTypeName()

@KotlinPoetJavaPoetPreview
internal fun KTypeName.toJavaTypeName(boxIfPrimitive: Boolean = false) =
    toJTypeName(boxIfPrimitive).let {
        when {
            it.toString() == "kotlin.Unit" -> JTypeName.VOID
            it.toString() == "kotlin.CharSequence" ->
                JClassName.bestGuess("java.lang.CharSequence")
            else -> it
        }
    }

@KotlinPoetJavaPoetPreview
@KotlinPoetKspPreview
internal fun KSTypeParameter.toJavaTypeVariableName(): JTypeVariableName {
    return toTypeVariableName().run {
        JTypeVariableName.get(
            name,
            *bounds.map { it.toJavaTypeName(boxIfPrimitive = true) }.toTypedArray()
        )
    }
}

@KotlinPoetJavaPoetPreview
@KotlinPoetKspPreview
internal fun KSValueParameter.toJParameterSpec(
    typeParamResolver: TypeParameterResolver = TypeParameterResolver.EMPTY
): JParameterSpec {
    val variableName = name?.asString()
    var variableTypeName = type.toTypeName(typeParamResolver).toJavaTypeName()
    if (isVararg && variableTypeName !is ArrayTypeName) {
        variableTypeName = ArrayTypeName.of(variableTypeName)
    }
    return JParameterSpec.builder(variableTypeName, variableName).build()
}

@KspExperimental
@KotlinPoetKspPreview
internal fun KSValueParameter.toKParameterSpec(
    typeParamResolver: TypeParameterResolver = TypeParameterResolver.EMPTY
): ParameterSpec {
    val variableName = name!!.asString()
    val isNullable = getAnnotationsByType(Nullable::class).firstOrNull() != null
    var typeName = type.toTypeName(typeParamResolver)
    if (isNullable) typeName = typeName.copy(true)
    return ParameterSpec.builder(variableName, typeName).apply {
        if (isVararg)
            addModifiers(KModifier.VARARG)
        if (isNoInline)
            addModifiers(KModifier.NOINLINE)
        if (isCrossInline)
            addModifiers(KModifier.CROSSINLINE)
    }.build()
}

internal fun KSNode.isJava() = origin == Origin.JAVA || origin == Origin.JAVA_LIB

internal fun KSNode.isKotlin() = origin == Origin.KOTLIN || origin == Origin.KOTLIN_LIB

internal fun KSClassDeclaration.superclass(): KSTypeReference? {
    return superTypes.find {
        val declaration = it.resolve().declaration
        (declaration as? KSClassDeclaration)?.classKind == ClassKind.CLASS
    }
}

fun Sequence<KSFunctionDeclaration>.findTypeArgumentConstructorFun(typeParametersSize: Int): KSFunctionDeclaration? {
    for (it in this) {
        if (!it.isPublic()) continue
        it.parameters.forEach { variableElement ->
            if ("java.lang.reflect.Type[]" == variableElement.type.getQualifiedName())
                return it
        }
        //构造方法参数个数小于泛型个数，则遍历下一个
        if (it.parameters.size < typeParametersSize) continue
        for (i in 0 until typeParametersSize) {
            //参数非java.lang.reflect.Type，返回null
            if ("java.lang.reflect.Type" != it.parameters[i].type.getQualifiedName()) {
                return null
            }
        }
        return it
    }
    return null
}

internal fun KSClassDeclaration?.instanceOf(className: String): Boolean {
    if (this == null) return false
    if (className == qualifiedName?.asString()) return true
    superTypes.forEach {
        val ksClass = it.resolve().declaration as? KSClassDeclaration
        if (ksClass.instanceOf(className)) return true
    }
    return false
}

@KspExperimental
internal fun KSPropertyDeclaration.inStaticToJava(): Boolean {
    return getAnnotationsByType(JvmField::class).firstOrNull() != null
            || Modifier.CONST in modifiers
}

internal fun KSPLogger.error(throwable: Throwable, ksNode: KSNode) {
    error(throwable.message ?: "", ksNode)
}


internal fun String.firstLetterUpperCase(): String {
    val charArray = toCharArray()
    val firstChar = charArray.firstOrNull() ?: return this
    if (firstChar.code in 97..122) {
        charArray[0] = firstChar.minus(32)
    }
    return String(charArray)
}

internal fun JavaFile.writeTo(
    codeGenerator: CodeGenerator,
    dependencies: Dependencies = Dependencies(false)
) {
    val fos = codeGenerator.createNewFile(dependencies, packageName, typeSpec.name, "java")
    fos.bufferedWriter(Charsets.UTF_8).use(this::writeTo)
}