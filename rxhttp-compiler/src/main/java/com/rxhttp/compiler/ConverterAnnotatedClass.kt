package com.rxhttp.compiler

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import rxhttp.wrapper.annotation.Converter
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class ConverterAnnotatedClass {

    private val mElementMap = LinkedHashMap<String, VariableElement>()

    fun add(variableElement: VariableElement) {
        val annotation = variableElement.getAnnotation(Converter::class.java)
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
                    .addStatement(
                        """
                        return setConverter(${"$"}T.${"$"}L)
                    """.trimIndent(),
                        ClassName.get(value.enclosingElement.asType()),
                        value.simpleName.toString()
                    )
                    .returns(r)
                    .build()
                )
            }

            val converterName = ClassName.get("rxhttp.wrapper.callback", "IConverter")
            methodList.add(
                MethodSpec.methodBuilder("setConverter")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(converterName, "converter")
                    .addCode("""
                          if (converter == null)
                              throw new IllegalArgumentException("converter can not be null");
                          this.converter = converter;
                          return (R) this;
                    """.trimIndent())
                    .returns(r)
                    .build()
            )

            methodList.add(
                MethodSpec.methodBuilder("setConverterToParam")
                    .addJavadoc("给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用\n")
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(converterName, "converter")
                    .addStatement("param.tag(IConverter.class, converter)")
                    .addStatement("return (R)this")
                    .returns(r)
                    .build()
            )
            return methodList
        }
}