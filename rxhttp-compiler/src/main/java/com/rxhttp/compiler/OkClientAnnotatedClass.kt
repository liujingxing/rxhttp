package com.rxhttp.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import org.jetbrains.annotations.NotNull
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

            val okClientName = ClassName.get("okhttp3", "OkHttpClient")
            val okClientParam = ParameterSpec.builder(okClientName, "okClient")
                .addAnnotation(NotNull::class.java).build()

            methodList.add(MethodSpec.methodBuilder("setOkClient")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(okClientParam)
                .addCode("""
                    if (okClient == null) 
                        throw new IllegalArgumentException("okClient can not be null");
                    this.okClient = okClient;
                    return (R)this;
                """.trimIndent())
                .returns(r)
                .build())
            for ((key, value) in mElementMap) {
                val className = ClassName.get(value.enclosingElement.asType())
                val fieldName = value.simpleName.toString()
                methodList.add(MethodSpec.methodBuilder("set$key")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return setOkClient(\$T.\$L)", className, fieldName)
                    .returns(r)
                    .build()
                )
            }
            return methodList
        }
}