package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeVariable

//将Java的基本类型/常用类型转换为kotlin中对应的类型
fun TypeName.toKClassTypeName(): TypeName =
    when {
        this is ParameterizedTypeName -> { //带有具体泛型的类，如 kotlin.Array<kotlin.Int>
            val kClassRawType = rawType.toKClassTypeName()
            val realRawType =
                if ("kotlin.Array" == kClassRawType.toString()) {
                    when (typeArguments[0].toString()) {
                        "kotlin.Boolean" -> BOOLEAN_ARRAY
                        "kotlin.Byte" -> BYTE_ARRAY
                        "kotlin.Char" -> CHAR_ARRAY
                        "kotlin.Short" -> SHORT_ARRAY
                        "kotlin.Int" -> INT_ARRAY
                        "kotlin.Long" -> LONG_ARRAY
                        "kotlin.Float" -> FLOAT_ARRAY
                        "kotlin.Double" -> DOUBLE_ARRAY
                        else -> null
                    }
                } else null
            realRawType ?: (kClassRawType as ClassName).parameterizedBy(
                *typeArguments.map { it.toKClassTypeName() }
                    .toTypedArray()
            )
        }
        this is WildcardTypeName -> {
            //通配符
            when {
                this.toString() == "*" -> this
                inTypes.isNotEmpty() -> {
                    WildcardTypeName.consumerOf(inTypes[0].toKClassTypeName())
                }
                outTypes.isNotEmpty() -> {
                    WildcardTypeName.producerOf(outTypes[0].toKClassTypeName())
                }
                else -> this
            }
        }
        this is TypeVariableName -> {
            //泛型
            val newBounds = ArrayList<TypeName>()
            bounds.forEach {
                if (it.toString() != "java.lang.Object")
                    newBounds.add(it.toKClassTypeName())
            }
            TypeVariableName.invoke(name, newBounds)
        }
        toString() == "java.lang.Object" -> ANY
        toString() == "java.lang.String" -> STRING
        toString() == "java.lang.Number" -> NUMBER
        toString() == "java.lang.Boolean" -> BOOLEAN
        toString() == "java.lang.Byte" -> BYTE
        toString() == "java.lang.Short" -> SHORT
        toString() == "java.lang.Integer" -> INT
        toString() == "java.lang.Long" -> LONG
        toString() == "java.lang.Float" -> FLOAT
        toString() == "java.lang.Double" -> DOUBLE
        toString() == "java.lang.Character" -> CHAR
        toString() == "java.lang.CharSequence" -> CHAR_SEQUENCE
        toString() == "java.util.List" -> LIST
        toString() == "java.util.Map" -> MAP
        else -> this
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

fun List<ExecutableElement>.findNoArgumentConstructorFun(): ExecutableElement? {
    forEach {
        if (it.parameters.size == 0) return it
    }
    return null
}

fun List<ExecutableElement>.findTypeArgumentConstructorFun(typeParametersSize: Int): ExecutableElement? {
    for (it in this) {
        if (!it.modifiers.contains(Modifier.PUBLIC)) continue
        it.parameters.forEach { variableElement ->
            if (variableElement.asType().toString() == "java.lang.reflect.Type[]")
                return it
        }
        //构造方法参数个数小于泛型个数，则遍历下一个
        if (it.parameters.size < typeParametersSize) continue
        for (i in 0 until typeParametersSize) {
            //参数非java.lang.reflect.Type，返回null
            if (it.parameters[i].asType().toString() != "java.lang.reflect.Type") {
                return null
            }
        }
        return it
    }
    return null
}