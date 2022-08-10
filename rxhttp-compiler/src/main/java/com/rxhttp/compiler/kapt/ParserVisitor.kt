package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.getClassName
import com.rxhttp.compiler.isDependenceRxJava
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
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
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

        //获取自定义的解析器
        for ((parserAlias, typeElement) in elementMap) {
            //生成kotlin编写的asXxx/toXxx/toFlowXxx方法
            rxHttpExtensions.generateRxHttpExtendFun(typeElement, parserAlias)
            if (isDependenceRxJava()) {
                //依赖了RxJava，则生成Java语言编写的asXxx方法
                methodList.addAll(typeElement.getAsXxxFun(parserAlias, typeMap))
            }
        }
        rxHttpExtensions.generateClassFile(filer)
        return methodList
    }
}

//生成Java语言编写的asXxx方法
private fun TypeElement.getAsXxxFun(
    parserAlias: String,
    typeMap: LinkedHashMap<String, List<ClassName>>
): List<MethodSpec> {
    val methodList = ArrayList<MethodSpec>()

    //onParser方法返回类型
    val onParserFunReturnType = getOnParserFunReturnType() ?: return emptyList()

    val typeVariableNames = typeParameters.map { TypeVariableName.get(it) }

    //遍历public构造方法
    for (constructor in getPublicConstructors()) {
        //泛型数量
        var typeCount = typeVariableNames.size
        if ("java.lang.reflect.Type[]" ==
            constructor.parameters.firstOrNull()?.asType()?.toString()
        ) {
            //如果是Type是数组传递的，一个参数就行
            typeCount = 1
        }
        if (constructor.parameters.size < typeCount) continue

        //根据构造方法参数，获取asXxx方法需要的参数
        val parameterList = constructor.getParameterSpecs(typeVariableNames)

        //方法名
        val methodName = "as$parserAlias"
        //方法体
        val methodBody =
            "return asParser(new \$T${getTypeVariableString(typeVariableNames)}(${
                getParamsName(constructor.parameters, parameterList, typeVariableNames.size)
            }))"

        //生成的as方法返回类型(Observable<T>类型)
        val asFunReturnType = ParameterizedTypeName.get(
            getClassName("Observable"), onParserFunReturnType
        )

        val varargs = constructor.isVarArgs && parameterList.last().type is ArrayTypeName
        val parserClassName = ClassName.get(this)

        val methodSpec = MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariableNames)
            .addParameters(parameterList)
            .varargs(varargs)
            .addStatement(methodBody, parserClassName)  //方法里面的表达式
            .returns(asFunReturnType)
            .build()
            .apply { methodList.add(this) }

        val haveClassTypeParam = parameterList.any { p ->
            p.type.toString().startsWith("java.lang.Class")
        }

        //注意，这里获取泛型边界跟ksp不一样，这里会自动过滤Object类型，即使手动声明了
        if (haveClassTypeParam && typeVariableNames.size == 1 && typeVariableNames.first().bounds.isEmpty()) {
            //有Class类型参数 且 泛型数量等于1 且没有为泛型指定边界(Object类型边界除外)，才去生成Parser注解里wrappers字段对应的asXxx方法
            constructor.getAsXxxFun(
                parserAlias, methodSpec, parserClassName,
                onParserFunReturnType, typeMap, methodList
            )
        }

    }
    return methodList
}


/**
 * 生成Parser注解里wrappers字段指定类对应的asXxx方法
 * @param parserAlias 解析器别名
 * @param methodSpec 解析器对应的asXxx方法，没有经过wrappers字段包裹前的
 * @param parserClassName 解析器对应的ClassName对象
 * @param onParserFunReturnType 解析器里onParser方法的返回类型
 * @param typeMap Parser注解里wrappers字段集合
 * @param methodList MethodSpecs
 */
private fun ExecutableElement.getAsXxxFun(
    parserAlias: String,
    methodSpec: MethodSpec,
    parserClassName: ClassName,
    onParserFunReturnType: TypeName,
    typeMap: LinkedHashMap<String, List<ClassName>>,
    methodList: MutableList<MethodSpec>
) {
    val parameterList = methodSpec.parameters
    val typeVariableNames = methodSpec.typeVariables

    val type = ClassName.get("java.lang.reflect", "Type")
    val parameterizedType = ClassName.get("rxhttp.wrapper.entity", "ParameterizedTypeImpl")

    val wrapperListClass = arrayListOf<ClassName>()
    typeMap[parserAlias]?.apply { wrapperListClass.addAll(this) }
    val listClassName = ClassName.get("java.util", "List")
    if (listClassName !in wrapperListClass) {
        wrapperListClass.add(0, listClassName)
    }

    wrapperListClass.forEach { wrapperClass ->

        //1、asXxx方法返回值
        val onParserFunReturnWrapperType =
            if (onParserFunReturnType is ParameterizedTypeName) {
                //返回类型有n个泛型，需要对每个泛型再次包装
                val typeNames = onParserFunReturnType.typeArguments.map { typeArg ->
                    ParameterizedTypeName.get(wrapperClass, typeArg)
                }
                ParameterizedTypeName.get(onParserFunReturnType.rawType, *typeNames.toTypedArray())
            } else {
                ParameterizedTypeName.get(wrapperClass, onParserFunReturnType)
            }
        val asFunReturnType =
            ParameterizedTypeName.get(getClassName("Observable"), onParserFunReturnWrapperType)

        //2、asXxx方法名
        val name = wrapperClass.toString()
        val simpleName = name.substring(name.lastIndexOf(".") + 1)
        val methodName = "as$parserAlias${simpleName}"

        //3、asXxx方法体
        val funBody = CodeBlock.builder()
        val paramsName = StringBuilder()
        //遍历参数，取出参数名
        parameterList.forEachIndexed { index, param ->
            if (index > 0) paramsName.append(", ")
            if (param.type.toString().startsWith("java.lang.Class")) {
                /*
                 * Class类型参数，需要进行再次包装，最后再取参数名
                 * 格式：Type tTypeList = ParameterizedTypeImpl.get(List.class, tType);
                 */
                val variableName = "${param.name}$simpleName"
                val expression =
                    "\$T $variableName = \$T.get($simpleName.class, ${param.name})"
                funBody.addStatement(expression, type, parameterizedType)
                val parameterType = parameters[index].asType()
                if ("java.lang.reflect.Type[]" == parameterType.toString()) {
                    paramsName.append("new Type[]{$variableName}")
                } else {
                    paramsName.append(variableName)
                }
            } else {
                paramsName.append(param.name)
            }
        }
        val returnStatement =
            "return asParser(new \$T${getTypeVariableString(typeVariableNames)}($paramsName))"
        funBody.addStatement(returnStatement, parserClassName)

        //4、生成asXxx方法
        MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariableNames)
            .addParameters(parameterList)
            .varargs(methodSpec.varargs)
            .addCode(funBody.build())  //方法里面的表达式
            .returns(asFunReturnType)
            .build()
            .apply { methodList.add(this) }
    }
}

private fun ExecutableElement.getParameterSpecs(
    typeVariableNames: List<TypeVariableName>
): List<ParameterSpec> {
    val parameterList = ArrayList<ParameterSpec>()
    var typeIndex = 0
    val className = ClassName.get(Class::class.java)
    parameters.forEach { variableElement ->
        val variableType = variableElement.asType()
        if (variableType.toString() == "java.lang.reflect.Type[]") {
            typeVariableNames.forEach { typeVariableName ->
                //Type类型参数转Class<T>类型
                val classTypeName =
                    ParameterizedTypeName.get(className, typeVariableName)
                val variableName =
                    "${typeVariableName.name.lowercase(Locale.getDefault())}Type"
                val parameterSpec =
                    ParameterSpec.builder(classTypeName, variableName).build()
                parameterList.add(parameterSpec)
            }
        } else if (variableType.toString() == "java.lang.reflect.Type"
            && typeIndex < typeVariableNames.size
        ) {
            //Type类型参数转Class<T>类型
            val classTypeName = ParameterizedTypeName.get(
                className, typeVariableNames[typeIndex++]
            )
            val variableName = variableElement.simpleName.toString()
            parameterList.add(
                ParameterSpec.builder(classTypeName, variableName).build()
            )
        } else {
            parameterList.add(ParameterSpec.get(variableElement))
        }
    }
    return parameterList
}

/**
 * @param variableElements 解析器构造方法参数列表
 * @param parameterSpecs 通过解析器构造方法参数列表转换而来的实际参数列表，parameterSpecs.size() >= variableElements.size()
 * @param typeCount 解析器泛型数量
 */
private fun getParamsName(
    variableElements: List<VariableElement>,
    parameterSpecs: List<ParameterSpec>,
    typeCount: Int
): String {
    val sb = StringBuilder()
    var paramIndex = 0
    var variableIndex = 0
    val variableSize = variableElements.size
    val paramSize = parameterSpecs.size
    while (paramIndex < paramSize && variableIndex < variableSize) {
        if (variableIndex > 0) sb.append(", ")
        val type = variableElements[variableIndex++].asType()
        if ("java.lang.reflect.Type[]" == type.toString()) {
            sb.append("new Type[]{")
            for (i in 0 until typeCount) {
                if (i > 0) sb.append(", ")
                sb.append(parameterSpecs[paramIndex++].name)
            }
            sb.append("}")
        } else
            sb.append(parameterSpecs[paramIndex++].name)
    }
    return sb.toString()
}

//获取泛型字符串 比如:<T> 、<K,V>等等
private fun getTypeVariableString(typeVariableNames: List<TypeVariableName>): String {
    return if (typeVariableNames.isNotEmpty()) "<>" else ""
}

//获取onParser方法返回类型
private fun TypeElement.getOnParserFunReturnType(): TypeName? {
    val function = enclosedElements.find {
        it is ExecutableElement   //是方法
            && it.getModifiers().contains(Modifier.PUBLIC)  //public修饰
            && !it.getModifiers().contains(Modifier.STATIC) //非静态
            && it.simpleName.toString() == "onParse"  //onParse方法
            && it.parameters.size == 1  //只有一个参数
            && TypeName.get(it.parameters[0].asType())
            .toString() == "okhttp3.Response"  //参数是okhttp3.Response类型
    } ?: return null
    return TypeName.get((function as ExecutableElement).returnType)
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

    val typeParameterList = typeParameters
    if (typeParameterList.size > 0) {
        //查找带 java.lang.reflect.Type 参数的构造方法
        val constructorFun = getPublicConstructors().filter { it.parameters.isNotEmpty() }
        val typeArgumentConstructorFun = constructorFun
            .findTypeArgumentConstructorFun(typeParameterList.size)
        if (typeArgumentConstructorFun == null) {
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

    val className = "rxhttp.wrapper.parse.Parser"
    if (!instanceOf(className, types)) {
        val msg =
            "The class '$elementQualifiedName' annotated with @${Parser::class.java.simpleName} must inherit from $className"
        throw NoSuchElementException(msg)
    }
}