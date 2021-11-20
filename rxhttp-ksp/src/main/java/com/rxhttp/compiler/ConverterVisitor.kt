package com.rxhttp.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Converter
import java.util.*

class ConverterVisitor(
    private val logger: KSPLogger
) : KSVisitorVoid() {

    private val elementMap = LinkedHashMap<String, KSPropertyDeclaration>()


    @OptIn(KspExperimental::class)
    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        try {
            property.checkConverterValidClass()
            val annotation = property.getAnnotationsByType(Converter::class).firstOrNull()
            var name = annotation?.name
            if (name.isNullOrBlank()) {
                name = property.simpleName.toString().firstLetterUpperCase()
            }
            elementMap[name] = property
        } catch (e: NoSuchElementException) {
            logger.error(e, property)
        }
    }

    @KspExperimental
    fun getMethodList(): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        for ((key, value) in elementMap) {
            val parent = value.parent
            var className = parent.toString()
            var fieldName = value.simpleName.asString()
            if (parent is KSClassDeclaration && parent.isCompanionObject) {
                //伴生对象需要额外处理 类名及字段名
                if (value.getAnnotationsByType(JvmField::class).toList().isEmpty()) {
                    //没有使用JvmField注解
                    fieldName = "$className.get${
                        fieldName.substring(0, 1).uppercase()
                    }${fieldName.substring(1)}()"
                }
                className = parent.parent.toString()
            }

            MethodSpec.methodBuilder("set$key")
                .addModifiers(JModifier.PUBLIC)
                .addStatement(
                    "return setConverter(\$T.${fieldName})",
                    ClassName.get(value.packageName.asString(), className),
                )
                .returns(r)
                .build()
                .apply { methodList.add(this) }
        }
        return methodList
    }
}

@Throws(KspException::class)
private fun KSPropertyDeclaration.checkConverterValidClass() {
    val simpleName = simpleName.asString()
    if (!isPublic()) {
        val msg =
            "The variable '$simpleName' must be public, please add @JvmField annotation if you use kotlin"
        throw NoSuchElementException(msg)
    }
    if (isJava() && Modifier.JAVA_STATIC !in modifiers) {
        throw NoSuchElementException("The variable '$simpleName' is not static")
    }
    val className = "rxhttp.wrapper.callback.IConverter"
    val ksClass = type.resolve().declaration as? KSClassDeclaration
    if (!ksClass.instanceOf(className)) {
        throw NoSuchElementException("The variable '$simpleName' is not a IConverter")
    }
}