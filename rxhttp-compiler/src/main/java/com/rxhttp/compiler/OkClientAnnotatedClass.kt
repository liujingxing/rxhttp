package com.rxhttp.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.OkClient
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class OkClientAnnotatedClass {

    private val mElementMap = LinkedHashMap<String, VariableElement>()

    fun add(variableElement: VariableElement) {
        val annotation = variableElement.getAnnotation(OkClient::class.java)
        var name = annotation.name
        if (name.isEmpty()) {
            name = variableElement.simpleName.toString()
        }
        mElementMap[name] = variableElement
    }

    val methodList: List<MethodSpec>
        get() {
            val methodList = ArrayList<MethodSpec>()
            for ((key, value) in mElementMap) {
                methodList.add(MethodSpec.methodBuilder("set$key")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("""
                            if (${"$"}T.${"$"}L == null)
                            throw new IllegalArgumentException("OkHttpClient can not be null");
                    """.trimIndent(), ClassName.get(value.enclosingElement.asType()), value.simpleName.toString())
                    .addStatement("this.okClient = \$T.\$L",
                        ClassName.get(value.enclosingElement.asType()),
                        value.simpleName.toString())
                    .addStatement("return (R)this")
                    .returns(r)
                    .build()
                )
            }
            return methodList
        }
}