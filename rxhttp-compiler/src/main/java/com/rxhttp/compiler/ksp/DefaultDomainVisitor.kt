package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeVariableName

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
    fun getFun(): FunSpec {
        val typeVariableR = TypeVariableName("R", rxhttpKClass.parameterizedBy("P", "R")) //泛型R
        val methodBuilder = FunSpec.builder("addDefaultDomainIfAbsent")
            .addKdoc("给Param设置默认域名(如果缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(KModifier.PRIVATE)
            .returns(typeVariableR)
        property?.let { ksProperty ->
            val memberName = ksProperty.toMemberName()
            methodBuilder.addCode("return setDomainIfAbsent(%M)", memberName)
        }
        return methodBuilder.build()
    }
}