package com.rxhttp.compiler.kapt

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Types

/**
 * User: ljx
 * Date: 2021/10/17
 * Time: 12:30
 */
class DefaultDomainVisitor(
    private val types: Types,
    private val logger: Messager
) {

    private var element: VariableElement? = null

    fun set(elements: Set<Element>) {
        try {
            if (elements.size > 1) {
                val msg = "@DefaultDomain annotations can only be used once"
                throw NoSuchElementException(msg)
            }
            (elements.firstOrNull() as? VariableElement)?.apply {
                checkVariableValidClass(types)
                element = this
            }
        } catch (e: NoSuchElementException) {
            logger.error(e.message, elements.first())
        }
    }

    //对url添加域名方法
    fun getMethod(): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addJavadoc("给Param设置默认域名(如果缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
        element?.apply {
            val className = ClassName.get(enclosingElement.asType())
            methodBuilder.addCode("setDomainIfAbsent(\$T.${simpleName});", className)
        }
        return methodBuilder.build()
    }
}