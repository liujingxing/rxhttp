package com.rxhttp.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import com.squareup.kotlinpoet.javapoet.JTypeName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toJTypeVariableName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import rxhttp.wrapper.annotation.Parser
import java.util.*
import javax.lang.model.type.MirroredTypesException

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

    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        try {
            classDeclaration.checkParserValidClass()
            val annotation = classDeclaration.getAnnotationsByType(Parser::class)
            var name = annotation.firstOrNull()?.name
            if (name.isNullOrBlank()) {
                name = classDeclaration.simpleName.toString()
            }
            ksClassMap[name] = classDeclaration
//        try {
//            annotation.wrappers
//        } catch (e: UndeclaredThrowableException) {
//            logger.warn("LJX " + e.undeclaredThrowable.message)
//        }
//        val classNames = annotation.wrappers.map {
//            ClassName.bestGuess(it.qualifiedName)
//        }
//        typeMap[name] = classNames
        } catch (e: NoSuchElementException) {
            logger.error(e, classDeclaration)
        }
    }

    @KspExperimental
    @KotlinPoetKspPreview
    @KotlinPoetJavaPoetPreview
    fun getMethodList(codeGenerator: CodeGenerator): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        val rxHttpExtensions = RxHttpExtensions()
        ksClassMap.forEach { (parserAlias, ksClass) ->
            rxHttpExtensions.generateRxHttpExtendFun(ksClass, parserAlias)
            if (isDependenceRxJava()) {
                //依赖了RxJava，则生成Java语言编写的asXxx方法
                methodList.addAll(ksClass.getAsXxxFun(parserAlias, classNameMap))
            }
        }
        rxHttpExtensions.generateClassFile(codeGenerator)
        return methodList
    }
}

@KotlinPoetKspPreview
@KotlinPoetJavaPoetPreview
private fun KSClassDeclaration.getAsXxxFun(
    parserAlias: String,
    typeMap: LinkedHashMap<String, List<ClassName>>
): List<MethodSpec> {

    val methodSpecs = arrayListOf<MethodSpec>()
    val onParserFunReturnType = findOnParserFunReturnType() ?: return emptyList()
    val typeVariableNames = typeParameters.map {
        it.toTypeVariableName().toJTypeVariableName()
    }
    //遍历public构造方法
    getConstructors().filter { it.isPublic() }.forEach {
        //根据构造方法参数，获取asXxx方法需要的参数
        val parameterSpecs =
            it.getParameterSpecs(typeVariableNames, typeParameters.toTypeParameterResolver())

        //方法名
        val methodName = "as$parserAlias"

        //返回类型(Observable<T>类型)
        val asFunReturnType = ParameterizedTypeName.get(
            getClassName("Observable"), onParserFunReturnType
        )

        //方法体
        val methodBody =
            "return asParser(new \$T${getTypeVariableString(typeVariableNames)}(${
                getParamsName(it.parameters, parameterSpecs, typeVariableNames.size)
            }))"

        //最后一个是否是可变参数
        val varargs = it.parameters.lastOrNull()?.isVararg == true &&
                parameterSpecs.last().type is ArrayTypeName

        val methodSpec = MethodSpec.methodBuilder(methodName)
            .addModifiers(JModifier.PUBLIC)
            .addTypeVariables(typeVariableNames)
            .addParameters(parameterSpecs)
            .varargs(varargs)
            .addStatement(methodBody, toJClassName())  //方法体
            .returns(asFunReturnType)
            .build()
            .apply { methodSpecs.add(this) }

        val haveClassTypeParam = parameterSpecs.find { p ->
            p.type.toString().startsWith("java.lang.Class")
        } != null

        if (haveClassTypeParam && typeVariableNames.size == 1) {
            //有Class类型参数 且 泛型数量等于1 ，才去生成Parser注解里wrappers字段对应的asXxx方法
            it.getAsXxxFun(parserAlias, methodSpec, onParserFunReturnType, typeMap, methodSpecs)
        }
    }

    return methodSpecs
}

/**
 * 生成Parser注解里wrappers字段指定类对应的asXxx方法
 * @param parserAlias 解析器别名
 * @param methodSpec 解析器对应的asXxx方法，没有经过wrappers字段包裹前的
 * @param onParserFunReturnType 解析器里onParser方法的返回类型
 * @param typeMap Parser注解里wrappers字段集合
 * @param methodSpecs MethodSpecs
 */
@KotlinPoetKspPreview
@KotlinPoetJavaPoetPreview
private fun KSFunctionDeclaration.getAsXxxFun(
    parserAlias: String,
    methodSpec: MethodSpec,
    onParserFunReturnType: TypeName,
    typeMap: LinkedHashMap<String, List<ClassName>>,
    methodSpecs: MutableList<MethodSpec>
) {
    val parameterSpecs = methodSpec.parameters
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
        val asFunReturnType = ParameterizedTypeName.get(
            getClassName("Observable"),
            onParserFunReturnWrapperType
        )

        //2、asXxx方法名
        val name = wrapperClass.toString()
        val simpleName = name.substring(name.lastIndexOf(".") + 1)
        val methodName = "as$parserAlias${simpleName}"

        //3、asXxx方法体
        val funBody = CodeBlock.builder()
        val paramsName = StringBuilder()
        //遍历参数，取出参数名
        parameterSpecs.forEachIndexed { index, param ->
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
                val parameterType = parameters[index].name?.asString()
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
        funBody.addStatement(
            returnStatement, (parent as KSClassDeclaration).toJClassName()
        )

        //4、生成asXxx方法
        MethodSpec.methodBuilder(methodName)
            .addModifiers(JModifier.PUBLIC)
            .addTypeVariables(typeVariableNames)
            .addParameters(methodSpec.parameters)
            .varargs(methodSpec.varargs)
            .addCode(funBody.build())  //方法里面的表达式
            .returns(asFunReturnType)
            .build()
            .apply { methodSpecs.add(this) }
    }
}

//获取onParser方法返回类型
@KotlinPoetKspPreview
@KotlinPoetJavaPoetPreview
private fun KSClassDeclaration.findOnParserFunReturnType(): JTypeName? {
    val ksFunction = getAllFunctions().find {
        it.isPublic() &&
                !it.modifiers.contains(Modifier.JAVA_STATIC) &&
                it.getFunName() == "onParse" &&
                it.parameters.size == 1 &&
                it.parameters[0].type.getQualifiedName() == "okhttp3.Response"
    }
    return ksFunction?.returnType?.toJavaTypeName(typeParameters.toTypeParameterResolver())
}


@KotlinPoetKspPreview
@KotlinPoetJavaPoetPreview
private fun KSFunctionDeclaration.getParameterSpecs(
    typeVariableNames: List<TypeVariableName>,
    parent: TypeParameterResolver? = null,
): List<ParameterSpec> {
    val parameterList = ArrayList<ParameterSpec>()
    var typeIndex = 0
    val className = ClassName.get(Class::class.java)
    parameters.forEach { ksValueParameter ->
        val variableType = ksValueParameter.type.getQualifiedName()
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
            val variableName = ksValueParameter.name?.asString()
            val parameterSpec =
                ParameterSpec.builder(classTypeName, variableName).build()
            parameterList.add(parameterSpec)
        } else {
            val functionTypeParams = typeParameters.toTypeParameterResolver(parent)
            ksValueParameter.toJParameterSpec(functionTypeParams).apply {
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
        } else
            sb.append(parameterSpecs[paramIndex++].name)
    }
    return sb.toString()
}


//获取泛型字符串 比如:<T> 、<K,V>等等
private fun getTypeVariableString(typeVariableNames: List<TypeVariableName>): String {
    return if (typeVariableNames.isNotEmpty()) "<>" else ""
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

    val constructorFun = getConstructors().filter { it.isPublic() || it.isProtected() }
    if (typeParameters.isNotEmpty()) {
        //有泛型的解析器不能声明为final类型
        if (modifiers.contains(Modifier.FINAL)) {
            val msg = "This class '$elementQualifiedName' cannot be declared final"
            throw NoSuchElementException(msg)
        }
        //1、查找无参构造方法
        val noArgumentConstructorFun = constructorFun.find { it.parameters.isEmpty() }

        //未声明无参构造方法，抛出异常
        if (noArgumentConstructorFun == null) {
            val msg =
                "This class '$elementQualifiedName' must be declared 'protected $elementQualifiedName()' constructor method"
            throw NoSuchElementException(msg)
        }
        if (!noArgumentConstructorFun.modifiers.contains(Modifier.PROTECTED)) {
            //无参构造方法必须要声明为protected
            val msg =
                "This class '$elementQualifiedName' no-argument constructor must be declared protected"
            throw NoSuchElementException(msg)
        }

        if (isDependenceRxJava()) {
            //2、如果依赖了RxJava，则需要查找带 java.lang.reflect.Type 参数的构造方法
            val typeParameterList = typeParameters
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
    }

    val className = "rxhttp.wrapper.parse.Parser"
    if (!instanceOf(className)) {
        val msg =
            "The class '$elementQualifiedName' annotated with @${Parser::class.java.simpleName} must inherit from $className"
        throw NoSuchElementException(msg)
    }
}