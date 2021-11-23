package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
        property?.apply {
            val parent = parent
            var className = parent.toString()
            var fieldName = simpleName.asString()
            if (parent is KSClassDeclaration && parent.isCompanionObject) {
                //伴生对象需要额外处理 类名及字段名
                if (getAnnotationsByType(JvmField::class).toList().isEmpty()) {
                    //没有使用JvmField注解
                    fieldName = "$className.get${
                        fieldName.substring(0, 1).uppercase()
                    }${fieldName.substring(1)}()"
                }
                className = parent.parent.toString()
            }
            methodBuilder.addCode(
                """setDomainIfAbsent(${"$"}T.${fieldName});""",
                ClassName.get(packageName.asString(), className),
            )
        }
        return methodBuilder.build()
    }
}