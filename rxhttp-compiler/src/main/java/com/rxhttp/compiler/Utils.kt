package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeVariable

//将Java的基本类型/常用类型转换为kotlin中对应的类型
fun TypeName.toKClassTypeName(): TypeName =
    if (this is ParameterizedTypeName) {
        when (rawType.toString()) {
            else -> (rawType.toKClassTypeName() as ClassName).parameterizedBy(*typeArguments.map { it.toKClassTypeName() }.toTypedArray())
        }
    } else if (this is WildcardTypeName) {
        //通配符
        when {
            this.toString() == "*" -> {
                this
            }
            inTypes.isNotEmpty() -> {
                WildcardTypeName.consumerOf(inTypes[0].toKClassTypeName())
            }
            outTypes.isNotEmpty() -> {
                WildcardTypeName.producerOf(outTypes[0].toKClassTypeName())
            }
            else -> {
                this
            }
        }
    } else if (this is TypeVariableName) {
        //泛型
        val newBounds = ArrayList<TypeName>()
        bounds.forEach {
            if (it.toString() != "java.lang.Object")
                newBounds.add(it.toKClassTypeName())
        }
        TypeVariableName.invoke(name, newBounds)
    } else if (this.toString() == "java.lang.Object") ClassName("kotlin", "Any")
    else if (this.toString() == "java.lang.String") ClassName("kotlin", "String")
    else if (toString() == "java.lang.Number") ClassName("kotlin", "Number")
    else if (toString() == "java.lang.Boolean") ClassName("kotlin", "Boolean")
    else if (toString() == "java.lang.Byte") ClassName("kotlin", "Byte")
    else if (toString() == "java.lang.Short") ClassName("kotlin", "Short")
    else if (toString() == "java.lang.Integer") ClassName("kotlin", "Int")
    else if (toString() == "java.lang.Long") ClassName("kotlin", "Long")
    else if (toString() == "java.lang.Float") ClassName("kotlin", "Float")
    else if (toString() == "java.lang.Double") ClassName("kotlin", "Double")
    else if (toString() == "java.lang.CharSequence") ClassName("kotlin", "CharSequence")
    else if (toString() == "java.util.List") ClassName("kotlin.collections", "List")
    else if (toString() == "java.util.Map") ClassName("kotlin.collections", "Map")
    else {
        this
    }

//ExecutableElement 转 FunSpec.Builder
internal fun ExecutableElement.toFunSpecBuilder(): FunSpec.Builder {

    val methodName = simpleName.toString()
    val funBuilder = FunSpec.builder(methodName)
    funBuilder.jvmModifiers(modifiers) //方法修饰符

    typeParameters
        .map { it.asType() as TypeVariable }
        .map { it.asTypeVariableName().toKClassTypeName() }
        .forEach { funBuilder.addTypeVariable(it as TypeVariableName) }  //泛型

    funBuilder.returns(returnType.asTypeName())  //返回值
    parameters.forEach {
        val name = it.simpleName.toString()
        val type = it.asType().asTypeName().toKClassTypeName()
        val parameterSpec = ParameterSpec.builder(name, type)
            .jvmModifiers(it.modifiers)
            .build()

        funBuilder.addParameter(parameterSpec)
    }

    if (isVarArgs) { //处理可变参数(最后一个参数)
        val lastParameters = parameters.last()
        val asTypeName = lastParameters.asType().asTypeName()
        if (asTypeName is ParameterizedTypeName) {
            val type = asTypeName.typeArguments[0].toKClassTypeName()
            val parameterSpec = ParameterSpec.builder(lastParameters.simpleName.toString(), type)
                .jvmModifiers(lastParameters.modifiers)
                .addModifiers(KModifier.VARARG)
                .build()
            funBuilder.parameters[funBuilder.parameters.lastIndex] = parameterSpec
        }
    }

    if (thrownTypes.isNotEmpty()) {  //异常
        val throwsValueString = thrownTypes.joinToString { "%T::class" }
        funBuilder.addAnnotation(AnnotationSpec.builder(Throws::class)
            .addMember(throwsValueString, *thrownTypes.toTypedArray())
            .build())
    }

    return funBuilder
}