package com.rxhttp.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Domain
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class DomainAnnotatedClass {

    private val mElementMap = LinkedHashMap<String, VariableElement>()

    fun add(variableElement: VariableElement) {
        val annotation = variableElement.getAnnotation(Domain::class.java)
        var name: String = annotation.name
        if (name.isEmpty()) {
            name = variableElement.simpleName.toString()
        }
        mElementMap[name] = variableElement
    }

    //对url添加域名方法
    val methodList: List<MethodSpec>
        get() {
            val methodList = ArrayList<MethodSpec>()
            for ((key, value) in mElementMap) {
                methodList.add(MethodSpec.methodBuilder("setDomainTo" + key + "IfAbsent")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("String newUrl = addDomainIfAbsent(param.getSimpleUrl(), \$T.\$L)",
                        ClassName.get(value.enclosingElement.asType()),
                        value.simpleName.toString())
                    .addStatement("param.setUrl(newUrl)")
                    .addStatement("return (R)this")
                    .returns(r).build())
            }

            //对url添加域名方法
            methodList.add(
                MethodSpec.methodBuilder("addDomainIfAbsent")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .addParameter(String::class.java, "url")
                    .addParameter(String::class.java, "domain")
                    .addCode("""
                         if (url.startsWith("http")) return url;
                         if (url.startsWith("/")) {
                             if (domain.endsWith("/"))
                                 return domain + url.substring(1);
                             else
                                 return domain + url;
                         } else if (domain.endsWith("/")) {
                             return domain + url;
                         } else {
                             return domain + "/" + url;
                         }
                    """.trimIndent())
                    .returns(String::class.java).build())
            return methodList
        }
}