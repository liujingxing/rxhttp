package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.J_ARRAY_TYPE
import com.rxhttp.compiler.J_TYPE
import com.rxhttp.compiler.common.joinToStringIndexed
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxhttpClass
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import rxhttp.wrapper.annotation.Parser
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.util.Types

class ParserVisitor(private val logger: Messager) {

    private val elementMap = LinkedHashMap<String, TypeElement>()
    private val typeMap = LinkedHashMap<String, List<ClassName>>()

    fun add(element: TypeElement, types: Types) {
        try {
            element.checkParserValidClass(types)
            val annotation = element.getAnnotation(Parser::class.java)
            val name: String = annotation.name
            if (name.isBlank()) {
                val msg = "methodName() in @${Parser::class.java.simpleName} for class " +
                        "${element.qualifiedName} is null or empty! that's not allowed"
                throw NoSuchElementException(msg)
            }
            try {
                annotation.wrappers
            } catch (e: MirroredTypesException) {
                val typeMirrors = e.typeMirrors
                typeMap[name] = typeMirrors.map { ClassName.bestGuess(it.toString()) }
            }
            elementMap[name] = element
        } catch (e: NoSuchElementException) {
            logger.error(e.message, element)
        }
    }

    fun getMethodList(filer: Filer): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        val rxHttpExtensions = RxHttpExtensions()
        //遍历自定义解析器
        elementMap.forEach { (parserAlias, typeElement) ->
            //生成kotlin编写的toObservableXxx/toAwaitXxx/toFlowXxx方法
            rxHttpExtensions.generateRxHttpExtendFun(typeElement, parserAlias)
            //生成Java环境下toObservableXxx方法
            methodList.addAll(typeElement.getToObservableXxxFun(parserAlias, typeMap))
        }
        rxHttpExtensions.generateClassFile(filer)
        return methodList
    }
}

//生成Java语言编写的toObservableXxx方法
private fun TypeElement.getToObservableXxxFun(
    parserAlias: String,
    typeMap: LinkedHashMap<String, List<ClassName>>
): List<MethodSpec> {
    val methodList = ArrayList<MethodSpec>()
    //onParser方法返回类型
    val onParserFunReturnType = findOnParserFunReturnType() ?: return emptyList()
    val typeVariableNames = typeParameters.map { TypeVariableName.get(it) }
    val typeCount = typeVariableNames.size  //泛型数量
    val customParser = ClassName.get(this)

    //遍历构造方法
    for (constructor in getConstructors()) {
        if (!constructor.isValid(typeCount)) continue

        //原始参数
        val originParameterSpecs = constructor.parameters.map { ParameterSpec.get(it) }
        //将原始参数里的第一个Type数组或Type类型可变参数转换为n个Type类型参数，n为泛型数量
        val typeParameterSpecs = originParameterSpecs.flapTypeParameterSpecs(typeVariableNames)
        //将Type类型参数转换为Class<T>类型参数，有泛型时才转
        val classParameterSpecs = typeParameterSpecs.typeToClassParameterSpecs(typeVariableNames)

        //方法名
        val methodName = "toObservable$parserAlias"
        //返回类型(Observable<T>类型)
        val toObservableXxxFunReturnType = rxhttpClass.peerClass("ObservableCall")
            .parameterizedBy(onParserFunReturnType)

        val types = getTypeVariableString(typeVariableNames) // <T>, <K, V> 等
        //方法体
        val toObservableXxxFunBody =
            if (typeCount == 1 && onParserFunReturnType is TypeVariableName) {
                val paramNames = typeParameterSpecs.toParamNames()
                CodeBlock.of("return toObservable(wrap${customParser.simpleName()}($paramNames))")
            } else {
                val paramNames = typeParameterSpecs.toParamNames(originParameterSpecs, typeCount)
                CodeBlock.of("return toObservable(new \$T$types($paramNames))", customParser)
            }

        val varargs = constructor.isVarArgs && typeParameterSpecs.last().type is ArrayTypeName

        if (isDependenceRxJava()) {
            MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(typeVariableNames)
                .addParameters(typeParameterSpecs)
                .varargs(varargs)
                .addStatement(toObservableXxxFunBody)
                .returns(toObservableXxxFunReturnType)
                .build()
                .apply { methodList.add(this) }
        }

        if (typeCount == 1 && onParserFunReturnType is TypeVariableName) {
            val t = TypeVariableName.get("T")
            val typeUtil = ClassName.get("rxhttp.wrapper.utils", "TypeUtil")
            val okResponseParser = ClassName.get("rxhttp.wrapper.parse", "OkResponseParser")
            val parserClass = okResponseParser.peerClass("Parser").parameterizedBy(t)

            val suppressAnnotation = AnnotationSpec.builder(SuppressWarnings::class.java)
                .addMember("value", "\$S", "unchecked")
                .build()

            val firstParamName = typeParameterSpecs.first().name
            val paramNames = typeParameterSpecs.toParamNames(originParameterSpecs, typeCount)
                .replace(firstParamName, "actualType")

            MethodSpec.methodBuilder("wrap${customParser.simpleName()}")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(suppressAnnotation)
                .addTypeVariable(t)
                .addParameters(typeParameterSpecs)
                .varargs(varargs)
                .returns(parserClass)
                .addCode(
                    """
                    Type actualType = ${'$'}T.getActualType($firstParamName);
                    if (actualType == null) actualType = $firstParamName;
                    ${'$'}T parser = new ${'$'}T($paramNames);
                    return actualType == $firstParamName ? parser : new ${'$'}T(parser);
                """.trimIndent(), typeUtil, customParser, customParser, okResponseParser
                ).build().apply { methodList.add(this) }
        }

        if (typeCount > 0 && isDependenceRxJava()) {
            val paramNames = classParameterSpecs.toParamNames(typeCount)

            //生成Class类型参数的toObservableXxx方法
            val methodSpec = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(typeVariableNames)
                .addParameters(classParameterSpecs)
                .varargs(varargs)
                .addStatement("return $methodName($paramNames)")  //方法里面的表达式
                .returns(toObservableXxxFunReturnType)
                .build()
                .apply { methodList.add(this) }

            //注意，这里获取泛型边界跟ksp不一样，这里会自动过滤Object类型，即使手动声明了
            //泛型数量等于1 且没有为泛型指定边界(Object类型边界除外)，才去生成Parser注解里wrappers字段对应的toObservableXxx方法
            if (typeCount == 1 && typeVariableNames.first().bounds.isEmpty()) {
                val toObservableXxxFunList = methodSpec
                    .getToObservableXxxWrapFun(parserAlias, onParserFunReturnType, typeMap)
                methodList.addAll(toObservableXxxFunList)
            }
        }
    }
    return methodList
}

private fun List<ParameterSpec>.flapTypeParameterSpecs(
    typeVariableNames: List<TypeVariableName>
): List<ParameterSpec> {
    val parameterSpecs = mutableListOf<ParameterSpec>()
    forEachIndexed { index, parameterSpec ->
        val firstParamType = parameterSpec.type
        //对于kapt，数组类型或可变参数类型，得到的皆为数组类型，所以这里无需跟ksp一样判断可变参数类型
        if (index == 0 && firstParamType == J_ARRAY_TYPE && typeVariableNames.isNotEmpty()) {
            typeVariableNames.mapTo(parameterSpecs) {
                val variableName = "${it.name.lowercase(Locale.getDefault())}Type"
                ParameterSpec.builder(J_TYPE, variableName).build()
            }
        } else {
            parameterSpecs.add(parameterSpec)
        }
    }
    return parameterSpecs
}

private fun List<ParameterSpec>.typeToClassParameterSpecs(
    typeVariableNames: List<TypeVariableName>
): List<ParameterSpec> {
    val typeCount = typeVariableNames.size
    val className = ClassName.get(Class::class.java)
    return mapIndexed { index, parameterSpec ->
        if (index < typeCount) {
            val classType = className.parameterizedBy(typeVariableNames[index])
            ParameterSpec.builder(classType, parameterSpec.name).build()
        } else parameterSpec
    }
}

private fun List<ParameterSpec>.toParamNames(
    originParamsSpecs: List<ParameterSpec>,
    typeCount: Int
): String {
    val isArrayType = typeCount > 0 && originParamsSpecs.first().type == J_ARRAY_TYPE
    val paramNames = StringBuilder()
    if (isArrayType) {
        paramNames.append("new Type[]{")
    }
    forEachIndexed { index, parameterSpec ->
        if (index > 0) paramNames.append(", ")
        paramNames.append(parameterSpec.name)
        if (isArrayType && index == typeCount - 1) {
            paramNames.append("}")
        }
    }
    return paramNames.toString()
}

private fun List<ParameterSpec>.toParamNames(typeCount: Int): String {
    val paramNames = StringBuilder()
    forEachIndexed { index, parameterSpec ->
        if (index > 0) paramNames.append(", ")
        if (index < typeCount && parameterSpec.type.isClassType()) {
            paramNames.append("(Type) ")
        }
        paramNames.append(parameterSpec.name)
    }
    return paramNames.toString()
}

/**
 * 生成Parser注解里wrappers字段指定类对应的toObservableXxx方法
 * @param parserAlias 解析器别名
 * @param onParserFunReturnType 解析器里onParser方法的返回类型
 * @param typeMap Parser注解里wrappers字段集合
 */
private fun MethodSpec.getToObservableXxxWrapFun(
    parserAlias: String,
    onParserFunReturnType: TypeName,
    typeMap: LinkedHashMap<String, List<ClassName>>,
): List<MethodSpec> {
    val methodSpec = this //解析器对应的toObservableXxx方法，没有经过wrappers字段包裹前的
    val methodList = mutableListOf<MethodSpec>()
    val parameterSpecs = methodSpec.parameters
    val typeVariableNames = methodSpec.typeVariables
    val typeCount = typeVariableNames.size

    val parameterizedType = ClassName.get("rxhttp.wrapper.entity", "ParameterizedTypeImpl")

    val wrapperListClass = arrayListOf<ClassName>()
    typeMap[parserAlias]?.apply { wrapperListClass.addAll(this) }
    val listClassName = ClassName.get("java.util", "List")
    if (listClassName !in wrapperListClass) {
        wrapperListClass.add(0, listClassName)
    }
    wrapperListClass.forEach { wrapperClass ->

        //1、toObservableXxx方法返回值
        val onParserFunReturnWrapperType =
            if (onParserFunReturnType is ParameterizedTypeName) {
                //返回类型有n个泛型，需要对每个泛型再次包装
                val typeNames = onParserFunReturnType.typeArguments.map { typeArg ->
                    wrapperClass.parameterizedBy(typeArg)
                }
                onParserFunReturnType.rawType.parameterizedBy(*typeNames.toTypedArray())
            } else {
                wrapperClass.parameterizedBy(onParserFunReturnType)
            }
        val toObservableXxxFunReturnType = rxhttpClass.peerClass("ObservableCall")
            .parameterizedBy(onParserFunReturnWrapperType)

        //2、toObservableXxx方法名
        val name = wrapperClass.toString()
        val simpleName = name.substring(name.lastIndexOf(".") + 1)
        val methodName = "toObservable$parserAlias${simpleName}"

        //3、toObservableXxx方法体
        val funBody = CodeBlock.builder()
        val paramNames = parameterSpecs.joinToStringIndexed(", ") { index, it ->
            if (index < typeCount) {
                //Class类型参数，需要进行再次包装，最后再取参数名
                val variableName = "${it.name}$simpleName"
                //格式：Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
                val expression = "\$T $variableName = \$T.get($simpleName.class, ${it.name})"
                funBody.addStatement(expression, J_TYPE, parameterizedType)
                variableName
            } else it.name
        }
        val returnStatement = "return ${methodSpec.name}($paramNames)"
        funBody.addStatement(returnStatement)

        //4、生成toObservableXxx方法
        MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariableNames)
            .addParameters(parameterSpecs)
            .varargs(methodSpec.varargs)
            .addCode(funBody.build())  //方法里面的表达式
            .returns(toObservableXxxFunReturnType)
            .build()
            .apply { methodList.add(this) }
    }
    return methodList
}
private fun TypeName.isClassType() = toString().startsWith("java.lang.Class")

//获取泛型字符串 比如:<T> 、<K,V>等等
private fun getTypeVariableString(typeVariableNames: List<TypeVariableName>): String {
    return if (typeVariableNames.isNotEmpty()) "<>" else ""
}

@Throws(NoSuchElementException::class)
private fun TypeElement.checkParserValidClass(types: Types) {
    val elementQualifiedName = qualifiedName.toString()
    if (!modifiers.contains(Modifier.PUBLIC)) {
        throw NoSuchElementException("The class '$elementQualifiedName' must be public")
    }
    if (modifiers.contains(Modifier.ABSTRACT)) {
        val msg =
            "The class '$elementQualifiedName' is abstract. You can't annotate abstract classes with @${Parser::class.java.simpleName}"
        throw NoSuchElementException(msg)
    }

    val className = "rxhttp.wrapper.parse.Parser"
    if (!instanceOf(className, types)) {
        val msg =
            "The class '$elementQualifiedName' annotated with @${Parser::class.java.simpleName} must inherit from $className"
        throw NoSuchElementException(msg)
    }

    val typeParameterList = typeParameters
    val typeCount = typeParameterList.size
    if (typeCount > 0) {
        //查找带 java.lang.reflect.Type 参数的构造方法
        val isValid = getConstructors().any { it.isValid(typeCount) }
        if (!isValid) {
            val method = StringBuffer("public ${simpleName}(")
            for (i in typeParameterList.indices) {
                method.append("java.lang.reflect.Type")
                method.append(if (i == typeParameterList.lastIndex) ")" else ",")
            }
            val msg =
                "This class '$elementQualifiedName' must declare '$method' constructor method"
            throw NoSuchElementException(msg)
        }
    }
}