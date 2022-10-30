package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.FunSpec
import rxhttp.wrapper.annotation.Converter
import java.util.*

class ConverterVisitor(
    private val resolver: Resolver,
    private val logger: KSPLogger
) : KSVisitorVoid() {

    private val elementMap = LinkedHashMap<String, KSPropertyDeclaration>()

    @KspExperimental
    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        try {
            property.checkConverterProperty()
            val annotation = property.getAnnotationsByType(Converter::class).firstOrNull()
            var name = annotation?.name
            if (name.isNullOrBlank()) {
                name = property.simpleName.asString().firstLetterUpperCase()
            }
            if (elementMap.containsKey(name)) {
                val msg =
                    "The variable '${property.simpleName.asString()}' in the @Converter annotation 'name = $name' is duplicated"
                throw NoSuchElementException(msg)
            }
            elementMap[name] = property
        } catch (e: NoSuchElementException) {
            logger.error(e, property)
        }
    }

    fun getFunList(): List<FunSpec> {
        return elementMap.mapNotNull { entry ->
            val key = entry.key
            val ksProperty = entry.value
            val memberName = ksProperty.toMemberName()
            FunSpec.builder("set$key")
                .addCode("return setConverter(%M)", memberName)
                .build()
        }
    }
}

@Throws(NoSuchElementException::class)
private fun KSPropertyDeclaration.checkConverterProperty() {
    val variableName = simpleName.asString()

    val className = "rxhttp.wrapper.callback.IConverter"
    val ksClass = type.resolve().declaration as? KSClassDeclaration
    if (!ksClass.instanceOf(className)) {
        throw NoSuchElementException("The variable '$variableName' must be IConverter")
    }

    var curParent = parent
    while (curParent is KSClassDeclaration) {
        if (!curParent.isPublic()) {
            val msg = "The class '${curParent.qualifiedName?.asString()}' must be public"
            throw NoSuchElementException(msg)
        }
        curParent = curParent.parent
    }

    if (!isPublic()) {
        throw NoSuchElementException("The variable '$variableName' must be public")
    }

    if (isJava() && Modifier.JAVA_STATIC !in modifiers) {
        throw NoSuchElementException("The variable '$variableName' must be static")
    }

    if (isKotlin()) {
        val parent = parent
        //在kt文件里，说明是顶级变量，属于合法，直接返回
        if (parent is KSFile) return
        //在伴生对象里面，是合法的，直接返回
        if ((parent as? KSClassDeclaration)?.isCompanionObject == true) return

        if ((parent as? KSClassDeclaration)?.classKind != ClassKind.OBJECT) {
            //必需要声明在object对象里
            throw NoSuchElementException("The variable '$variableName' must be declared in the object")
        }
    }
}