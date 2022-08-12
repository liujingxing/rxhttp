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
import com.rxhttp.compiler.getKClassName
import com.rxhttp.compiler.isDependenceRxJava
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
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
    fun getFunList(codeGenerator: CodeGenerator): List<FunSpec> {
        val funList = ArrayList<FunSpec>()
        val rxHttpExtensions = RxHttpExtensions(logger)
        ksClassMap.forEach { (parserAlias, ksClass) ->
            rxHttpExtensions.generateRxHttpExtendFun(ksClass, parserAlias)
            if (isDependenceRxJava()) {
                //依赖了RxJava，则生成Java语言编写的asXxx方法
                funList.addAll(ksClass.getAsXxxFun(parserAlias, classNameMap))
            }
        }
        rxHttpExtensions.generateClassFile(codeGenerator)
        return funList
    }
}

@KspExperimental
private fun KSClassDeclaration.getAsXxxFun(
    parserAlias: String,
    typeMap: LinkedHashMap<String, List<ClassName>>
): List<FunSpec> {

    val funList = arrayListOf<FunSpec>()
    val onParserFunReturnType = findOnParserFunReturnType() ?: return emptyList()
    val typeVariableNames = typeParameters.map { it.toTypeVariableName() }
    //遍历public构造方法
    for (constructor in getPublicConstructors()) {
        var typeCount = typeVariableNames.size
        if ("kotlin.Array<java.lang.reflect.Type>" ==
            constructor.parameters.firstOrNull()?.type?.toTypeName()?.toString()
        ) {
            //如果是Type是数组传递的，一个参数就行
            typeCount = 1
        }
        if (constructor.parameters.size < typeCount) continue

        //根据构造方法参数，获取asXxx方法需要的参数
        val parameterSpecs =
            constructor.getParameterSpecs(
                typeVariableNames,
                typeParameters.toTypeParameterResolver()
            )

        //方法名
        val funName = "as$parserAlias"

        //返回类型(Observable<T>类型)
        val asFunReturnType = getKClassName("Observable").parameterizedBy(onParserFunReturnType)

        //方法体
        val funBody =
            "return asParser(%T(${
                getParamsName(constructor.parameters, parameterSpecs, typeVariableNames.size)
            }))"

        val funSpec = FunSpec.builder(funName)
            .addTypeVariables(typeVariableNames)
            .addParameters(parameterSpecs)
            .addStatement(funBody, toClassName())  //方法体
            .returns(asFunReturnType)
            .build()
            .apply { funList.add(this) }

        val haveClassTypeParam = parameterSpecs.any { p ->
            p.type.toString().startsWith("java.lang.Class")
        }

        if (haveClassTypeParam && typeVariableNames.size == 1) {
            //查找非Any类型参数
            val nonAnyType = typeVariableNames.first().bounds.find { typeName ->
                val name = typeName.toString()
                name != "kotlin.Any" && name != "kotlin.Any?"
            }
            //有Class类型参数 且 泛型数量等于1 且没有为泛型指定边界(Any类型边界除外)，才去生成Parser注解里wrappers字段对应的asXxx方法
            if (nonAnyType == null) {
                constructor.getAsXxxFun(
                    parserAlias,
                    funSpec,
                    onParserFunReturnType,
                    typeMap,
                    funList
                )
            }
        }
    }

    return funList
}

/**
 * 生成Parser注解里wrappers字段指定类对应的asXxx方法
 * @param parserAlias 解析器别名
 * @param funSpec 解析器对应的asXxx方法，没有经过wrappers字段包裹前的
 * @param onParserFunReturnType 解析器里onParser方法的返回类型
 * @param typeMap Parser注解里wrappers字段集合
 * @param funList funList
 */
private fun KSFunctionDeclaration.getAsXxxFun(
    parserAlias: String,
    funSpec: FunSpec,
    onParserFunReturnType: TypeName,
    typeMap: LinkedHashMap<String, List<ClassName>>,
    funList: MutableList<FunSpec>
) {
    val parameterSpecs = funSpec.parameters
    val typeVariableNames = funSpec.typeVariables

    val type = ClassName("java.lang.reflect", "Type")
    val parameterizedType = ClassName("rxhttp.wrapper.entity", "ParameterizedTypeImpl")

    val wrapperListClass = arrayListOf<ClassName>()
    typeMap[parserAlias]?.apply { wrapperListClass.addAll(this) }
    if (LIST !in wrapperListClass) {
        wrapperListClass.add(0, LIST)
    }
    wrapperListClass.forEach { wrapperClass ->

        //1、asXxx方法返回值
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
        val asFunReturnType =
            getKClassName("Observable").parameterizedBy(onParserFunReturnWrapperType.copy(onParserFunReturnType.isNullable))

        //2、asXxx方法名
        val name = wrapperClass.toString()
        val simpleName = name.substring(name.lastIndexOf(".") + 1)
        val funName = "as$parserAlias${simpleName}"

        //3、asXxx方法体
        val funBody = CodeBlock.builder()
        val paramsName = StringBuilder()
        //遍历参数，取出参数名
        parameterSpecs.forEachIndexed { index, param ->
            if (index > 0) paramsName.append(", ")
            if (param.type.toString().startsWith("java.lang.Class")) {
                /*
                 * Class类型参数，需要进行再次包装，最后再取参数名
                 * 格式：val tTypeList = ParameterizedTypeImpl.get(List.class, tType)
                 */
                val variableName = "${param.name}$simpleName"
                val expression =
                    "val $variableName = %T.get($simpleName::class.java, ${param.name})"
                funBody.addStatement(expression, parameterizedType)
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
        val returnStatement = "return asParser(%T($paramsName))"
        funBody.addStatement(
            returnStatement, (parent as KSClassDeclaration).toClassName()
        )

        //4、生成asXxx方法
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


@KspExperimental
private fun KSFunctionDeclaration.getParameterSpecs(
    typeVariableNames: List<TypeVariableName>,
    parent: TypeParameterResolver? = null,
): List<ParameterSpec> {
    val parameterList = ArrayList<ParameterSpec>()
    var typeIndex = 0
    val className = Class::class.asClassName()
    parameters.forEach { ksValueParameter ->
        val variableType = ksValueParameter.type.getQualifiedName()
        if (variableType.toString() == "java.lang.reflect.Type[]") {
            typeVariableNames.forEach { typeVariableName ->
                //Type类型参数转Class<T>类型
                val classTypeName = className.parameterizedBy(typeVariableName)
                val variableName =
                    "${typeVariableName.name.lowercase(Locale.getDefault())}Type"
                val parameterSpec =
                    ParameterSpec.builder(variableName, classTypeName).build()
                parameterList.add(parameterSpec)
            }
        } else if (variableType.toString() == "java.lang.reflect.Type"
            && typeIndex < typeVariableNames.size
        ) {
            //Type类型参数转Class<T>类型
            val classTypeName = className.parameterizedBy(typeVariableNames[typeIndex++])
            val variableName = ksValueParameter.name?.asString().toString()
            val parameterSpec =
                ParameterSpec.builder(variableName, classTypeName).build()
            parameterList.add(parameterSpec)
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
    typeCount: Int
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
        }
    }
    return sb.toString()
}


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