package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Modifier

/**
 * User: ljx
 * Date: 2021/10/17
 * Time: 12:30
 */
class DefaultDomainVisitor(
    private val resolver: Resolver,
    private val logger: KSPLogger
) : KSVisitorVoid() {

    private var property: KSPropertyDeclaration? = null

    fun originatingFile() = property?.containingFile

    @OptIn(KspExperimental::class)
    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        try {
            if (this.property != null) {
                val msg = "@DefaultDomain annotations can only be used once"
                throw NoSuchElementException(msg)
            }
            property.checkDomainProperty()
            this.property = property
        } catch (e: NoSuchElementException) {
            logger.error(e, property)
        }
    }

    //对url添加域名方法
    @KspExperimental
    fun getMethod(): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addJavadoc("给Param设置默认域名(如果缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
        property?.getClassAndFieldName(resolver)?.apply {
            val className = first
            val fieldName = second
            methodBuilder.addCode(
                "setDomainIfAbsent(\$T.$fieldName);", ClassName.bestGuess(className),
            )
        }
        return methodBuilder.build()
    }
}