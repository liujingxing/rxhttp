package com.rxhttp.compiler

import com.rxhttp.compiler.exception.ProcessingException
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

/**
 * User: ljx
 * Date: 2021/10/17
 * Time: 12:30
 */
class DefaultDomainVisitor {

    private var element: VariableElement? = null

    fun set(elements: Set<Element>) {
        if (elements.size > 1) {
            throw ProcessingException(
                elements.iterator().next(),
                "@DefaultDomain annotations can only be used once"
            )
        } else if (elements.isNotEmpty()) {
            val variableElement = elements.first() as VariableElement
            checkVariableValidClass(variableElement)
            element = variableElement
        }
    }

    //对url添加域名方法
    fun getMethod(): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addJavadoc("给Param设置默认域名(如果缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(p, "param")
        element?.apply {
            methodBuilder.addCode(
                """
                String newUrl = addDomainIfAbsent(param.getSimpleUrl(), ${"$"}T.${simpleName});
                param.setUrl(newUrl);
                return param;
            """.trimIndent(),
                ClassName.get(enclosingElement.asType())
            )
        } ?: methodBuilder.addCode("return param;")
        methodBuilder.returns(p)
        return methodBuilder.build()
    }
}