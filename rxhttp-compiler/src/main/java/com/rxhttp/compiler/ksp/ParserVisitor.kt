package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KSTypesNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.rxhttp.compiler.common.getTypeVariableString
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import rxhttp.wrapper.annotation.Parser
import java.util.*

/**
 * User: ljx
 * Date: 2021/10/17
 * Time: 22:33
 */
class ParserVisitor(
    private val logger: KSPLogger
) : KSVisitorVoid() {

    private val ksClassMap = LinkedHashMap<String, KSClassDeclaration>()
    private val classNameMap = LinkedHashMap<String, List<ClassName>>()

    @KspExperimental
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        try {
            classDeclaration.checkParserValidClass()
            val annotation = classDeclaration.getAnnotationsByType(Parser::class).firstOrNull()
            var name = annotation?.name
            if (name.isNullOrBlank()) {
                name = classDeclaration.simpleName.toString()
            }
            ksClassMap[name] = classDeclaration
            val classNames =
                try {
                    annotation?.wrappers?.map { it.java.asClassName() }
                } catch (e: KSTypesNotPresentException) {
                    e.ksTypes.map {
                        ClassName.bestGuess(it.declaration.qualifiedName?.asString().toString())
                    }
                }
            classNames?.let { classNameMap[name] = it }

        } catch (e: NoSuchElementException) {
            logger.error(e, classDeclaration)
        }
    }

    @KspExperimental
    fun getFunList(codeGenerator: CodeGenerator, companionFunList: MutableList<FunSpec>): List<FunSpec> {
        val funList = ArrayList<FunSpec>()
        val rxHttpExtensions = RxHttpExtensions(logger)
        ksClassMap.forEach { (parserAlias, ksClass) ->
            rxHttpExtensions.generateRxHttpExtendFun(ksClass, parserAlias)
            //生成Java环境下toObservableXxx方法
            funList.addAll(ksClass.getToObservableXxxFun(parserAlias, classNameMap, companionFunList))
        }
        rxHttpExtensions.generateClassFile(codeGenerator)
        return funList
    }
}

@KspExperimental
private fun KSClassDeclaration.getToObservableXxxFun(
    parserAlias: String,
    typeMap: LinkedHashMap<String, List<ClassName>>,
    companionFunList: MutableList<FunSpec>,
): List<FunSpec> {
    val funList = arrayListOf<FunSpec>()
    //onParser方法返回类型
    val onParserFunReturnType = findOnParserFunReturnType() ?: return emptyList()
    val typeVariableNames = typeParameters.map { it.toTypeVariableName() }
    val typeCount = typeVariableNames.size  //泛型数量
    val customParser = toClassName()

    //遍历public构造方法
    for (constructor in getPublicConstructors()) {
        //参数为空，说明该构造方法无效
        constructor.getParametersIfValid(typeCount) ?: continue

        //根据构造方法参数，获取toObservableXxx方法需要的参数
        val parameterSpecs = constructor.getParameterSpecs(
            typeVariableNames,
            typeParameters.toTypeParameterResolver()
        )

        //方法名
        val funName = "toObservable$parserAlias"
        //返回类型(Observable<T>类型)
        val toObservableXxxFunReturnType = rxhttpKClass.peerClass("ObservableCall")
            .parameterizedBy(onParserFunReturnType)

        val wrapCustomParser = MemberName(rxHttpPackage, "wrap${customParser.simpleName}")
        val types = getTypeVariableString(typeVariableNames) // <T>, <K, V> 等

        //参数名
        val paramsName = getParamsName(constructor.parameters, parameterSpecs, typeCount)
        //方法体
        val toObservableXxxFunBody = if (typeCount == 1) {
            CodeBlock.of("return toObservable(%M$types($paramsName))", wrapCustomParser)
        } else {
            CodeBlock.of("return toObservable(%T$types($paramsName))", customParser)
        }

        val originParameters = constructor.parameters.map {
            val functionTypeParams =
                typeParameters.toTypeParameterResolver(typeParameters.toTypeParameterResolver())
            it.toKParameterSpec(functionTypeParams)
        }

        if (typeCount == 1) {
            val t = TypeVariableName("T")
            val typeUtil = ClassName("rxhttp.wrapper.utils", "TypeUtil")
            val okResponseParser = ClassName("rxhttp.wrapper.parse", "OkResponseParser")
            val parserClass = okResponseParser.peerClass("Parser").parameterizedBy(t)

            val suppressAnnotation = AnnotationSpec.builder(Suppress::class)
                .addMember("%S", "UNCHECKED_CAST")
                .build()

            val index = paramsName.indexOf(",")
            val wrapParams = if (index != -1) ", ${paramsName.substring(index + 1)}" else ""

            FunSpec.builder("wrap${customParser.simpleName}")
                .addAnnotation(suppressAnnotation)
                .addTypeVariable(t)
                .addParameters(originParameters)
                .returns(parserClass)
                .addCode(
                    """
                    val actualType = %T.getActualType(type) ?: type
                    val parser = %T<Any>(actualType$wrapParams)
                    val actualParser = if (actualType == type) parser else %T(parser)
                    return actualParser as Parser<T>
                """.trimIndent(), typeUtil, customParser, okResponseParser
                )
                .build()
                .apply { companionFunList.add(this) }
        }

        if (isDependenceRxJava()) {
            FunSpec.builder(funName)
                .addTypeVariables(typeVariableNames)
                .addParameters(originParameters)
                .addCode(toObservableXxxFunBody)
                .build()
                .apply { funList.add(this) }
        }

        if (typeCount > 0 && isDependenceRxJava()) {
            val paramNames = getParamsName(constructor.parameters, parameterSpecs, typeCount, true)

            val funSpec = FunSpec.builder(funName)
                .addTypeVariables(typeVariableNames)
                .addParameters(parameterSpecs)
                .addStatement("return $funName($paramNames)")  //方法里面的表达式
                .returns(toObservableXxxFunReturnType)
                .build()
                .apply { funList.add(this) }

            val haveClassTypeParam = parameterSpecs.any { p -> p.type.isClassType() }

            if (haveClassTypeParam && typeCount == 1) {
                //查找非Any类型参数
                val nonAnyType = typeVariableNames.first().bounds.find { typeName ->
                    val name = typeName.toString()
                    name != "kotlin.Any" && name != "kotlin.Any?"
                }
                //有Class类型参数 且 泛型数量等于1 且没有为泛型指定边界(Any类型边界除外)，才去生成Parser注解里wrappers字段对应的toObservableXxx方法
                if (nonAnyType == null) {
                    constructor.getToObservableXxxFun(
                        parserAlias, funSpec, onParserFunReturnType, typeMap, funList
                    )
                }
            }
        }
    }
    return funList
}

//获取方法参数，如果该方法有效
fun KSFunctionDeclaration.getParametersIfValid(
    typeSize: Int
): List<KSValueParameter>? {
    val tempParameters = parameters
    var typeCount = typeSize //泛型数量
    val typeArray = "kotlin.Array<java.lang.reflect.Type>"
    if (typeArray == tempParameters.firstOrNull()?.type?.toTypeName()?.toString()) {
        typeCount = 1  //如果是Type是数组传递的，一个参数就行
    } else {
        //如果解析器有n个泛型，则构造方法前n个参数，必须是Type类型
        val match = tempParameters.subList(0, typeCount).all {
            "java.lang.reflect.Type" == it.type.getQualifiedName()
        }
        if (!match) return null
    }
    //构造方法参数数量小于泛型数量，直接过滤掉
    if (tempParameters.size < typeCount) return null
    return tempParameters.subList(typeCount, tempParameters.size)
}

/**
 * 生成Parser注解里wrappers字段指定类对应的toObservableXxx方法
 * @param parserAlias 解析器别名
 * @param funSpec 解析器对应的toObservableXxx方法，没有经过wrappers字段包裹前的
 * @param onParserFunReturnType 解析器里onParser方法的返回类型
 * @param typeMap Parser注解里wrappers字段集合
 * @param funList funList
 */
private fun KSFunctionDeclaration.getToObservableXxxFun(
    parserAlias: String,
    funSpec: FunSpec,
    onParserFunReturnType: TypeName,
    typeMap: LinkedHashMap<String, List<ClassName>>,
    funList: MutableList<FunSpec>
) {
    val parameterSpecs = funSpec.parameters
    val typeVariableNames = funSpec.typeVariables

    val wrapperListClass = arrayListOf<ClassName>()
    typeMap[parserAlias]?.apply { wrapperListClass.addAll(this) }
    if (LIST !in wrapperListClass) {
        wrapperListClass.add(0, LIST)
    }
    wrapperListClass.forEach { wrapperClass ->

        //1、toObservableXxx方法返回值
        val onParserFunReturnWrapperType =
            if (onParserFunReturnType is ParameterizedTypeName) { // List<T>, Map<K,V>等包含泛型的类
                //返回类型有n个泛型，需要对每个泛型再次包装
                val typeNames = onParserFunReturnType.typeArguments.map { typeArg ->
                    wrapperClass.parameterizedBy(typeArg)
                }
                onParserFunReturnType.rawType.parameterizedBy(*typeNames.toTypedArray())
            } else {
                wrapperClass.parameterizedBy(onParserFunReturnType.copy(false))
            }
        val asFunReturnType = rxhttpKClass.peerClass("ObservableCall")
            .parameterizedBy(onParserFunReturnWrapperType.copy(onParserFunReturnType.isNullable))

        //2、toObservableXxx方法名
        val name = wrapperClass.toString()
        val simpleName = name.substring(name.lastIndexOf(".") + 1)
        val funName = "toObservable$parserAlias${simpleName}"

        //3、toObservableXxx方法体
        val funBody = CodeBlock.builder()
        val paramsName = StringBuilder()
        //遍历参数，取出参数名
        parameterSpecs.forEachIndexed { index, param ->
            if (index > 0) paramsName.append(", ")
            if (param.type.isClassType()) {
                /*
                 * Class类型参数，需要进行再次包装，最后再取参数名
                 * 格式：val tTypeList = List::class.parameterizedBy(tType)
                 */
                val variableName = "${param.name}$simpleName"
                val expression = "val $variableName = $simpleName::class.parameterizedBy(${param.name})"
                funBody.addStatement(expression)
                val parameterType = parameters[index].name?.asString()
                if ("java.lang.reflect.Type[]" == parameterType.toString()) {
                    paramsName.append("new Type[]{$variableName}")
                } else {
                    paramsName.append(variableName)
                }
            } else {
                if (KModifier.VARARG in param.modifiers) paramsName.append("*")
                paramsName.append(param.name)
            }
        }
        val returnStatement = "return ${funSpec.name}($paramsName)"
        funBody.addStatement(returnStatement)

        //4、生成toObservableXxx方法
        FunSpec.builder(funName)
            .addTypeVariables(typeVariableNames)
            .addParameters(funSpec.parameters)
            .addCode(funBody.build())  //方法里面的表达式
            .returns(asFunReturnType)
            .build()
            .apply { funList.add(this) }
    }
}

//获取onParser方法返回类型
private fun KSClassDeclaration.findOnParserFunReturnType(): TypeName? {
    val ksFunction = getAllFunctions().find {
        it.isPublic() &&
                !it.modifiers.contains(Modifier.JAVA_STATIC) &&
                it.getFunName() == "onParse" &&
                it.parameters.size == 1 &&
                it.parameters[0].type.getQualifiedName() == "okhttp3.Response"
    }
    return ksFunction?.returnType?.toTypeName(typeParameters.toTypeParameterResolver())
}


//将解析器构造方法前n个Type类型参数转换为Class类型，其它参数类型不变，其中n为解析器泛型数量
@KspExperimental
private fun KSFunctionDeclaration.getParameterSpecs(
    typeVariableNames: List<TypeVariableName>,
    parent: TypeParameterResolver? = null,
): List<ParameterSpec> {
    val parameterList = ArrayList<ParameterSpec>()
    var typeIndex = 0
    val className = Class::class.asClassName()
    parameters.forEach { ksValueParameter ->
        val variableType = ksValueParameter.type.getQualifiedName()  //参数类型
        if (variableType.toString() == "java.lang.reflect.Type[]") {
            typeVariableNames.forEach { typeVariableName ->
                //Type类型参数转Class<T>类型
                val classT = className.parameterizedBy(typeVariableName)
                val variableName = "${typeVariableName.name.lowercase(Locale.getDefault())}Type"
                parameterList.add(ParameterSpec.builder(variableName, classT).build())
            }
        } else if (variableType.toString() == "java.lang.reflect.Type"
            && typeIndex < typeVariableNames.size
        ) {
            //Type类型参数转Class<T>类型
            val classT = className.parameterizedBy(typeVariableNames[typeIndex++])
            val variableName = ksValueParameter.name?.asString().toString()
            parameterList.add(ParameterSpec.builder(variableName, classT).build())
        } else {
            val functionTypeParams = typeParameters.toTypeParameterResolver(parent)
            ksValueParameter.toKParameterSpec(functionTypeParams).apply {
                parameterList.add(this)
            }
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
    variableElements: List<KSValueParameter>,
    parameterSpecs: List<ParameterSpec>,
    typeCount: Int,
    classToType: Boolean = false
): String {
    val sb = StringBuilder()
    var paramIndex = 0
    var variableIndex = 0
    val variableSize = variableElements.size
    val paramSize = parameterSpecs.size
    while (paramIndex < paramSize && variableIndex < variableSize) {
        if (variableIndex > 0) sb.append(", ")
        val type = variableElements[variableIndex++].type.getQualifiedName()
        if ("java.lang.reflect.Type[]" == type.toString()) {
            sb.append("new Type[]{")
            for (i in 0 until typeCount) {
                if (i > 0) sb.append(", ")
                sb.append(parameterSpecs[paramIndex++].name)
            }
            sb.append("}")
        } else {
            val parameterSpec = parameterSpecs[paramIndex++]
            if (KModifier.VARARG in parameterSpec.modifiers) sb.append("*")
            sb.append(parameterSpec.name)
            if (classToType && parameterSpec.type.isClassType()) {
                sb.append(" as Type")
            }
        }
    }
    return sb.toString()
}

private fun TypeName.isClassType() = toString().startsWith("java.lang.Class")


@Throws(NoSuchElementException::class)
private fun KSClassDeclaration.checkParserValidClass() {
    val elementQualifiedName = qualifiedName?.asString()
    if (!isPublic()) {
        throw NoSuchElementException("The class '$elementQualifiedName' must be public")
    }
    if (isAbstract()) {
        val msg =
            "The class '$elementQualifiedName' is abstract. You can't annotate abstract classes with @${Parser::class.java.simpleName}"
        throw NoSuchElementException(msg)
    }

    val typeParameterList = typeParameters
    if (typeParameterList.isNotEmpty()) {
        //查找带 java.lang.reflect.Type 参数的构造方法
        val constructorFun = getPublicConstructors().filter { it.parameters.isNotEmpty() }
        val typeArgumentConstructorFun = constructorFun
            .findTypeArgumentConstructorFun(typeParameterList.size)
        if (typeArgumentConstructorFun == null) {
            val funBody = StringBuffer("public ${simpleName.asString()}(")
            for (i in typeParameterList.indices) {
                funBody.append("java.lang.reflect.Type")
                funBody.append(if (i == typeParameterList.lastIndex) ")" else ",")
            }
            val msg =
                "This class '$elementQualifiedName' must declare '$funBody' constructor fun"
            throw NoSuchElementException(msg)
        }
    }

    val className = "rxhttp.wrapper.parse.Parser"
    if (!instanceOf(className)) {
        val msg =
            "The class '$elementQualifiedName' annotated with @${Parser::class.java.simpleName} must inherit from $className"
        throw NoSuchElementException(msg)
    }
}