package com.rxhttp.compiler

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Helper class to write tests, only used in Ksp Compile Testing tests, not a public API.
 */
open class AbstractTestSymbolProcessor(
    protected val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        return emptyList()
    }
}

// Would be nice if SymbolProcessorProvider was a fun interface
internal fun processorProviderOf(
    body: (environment: SymbolProcessorEnvironment) -> SymbolProcessor
): SymbolProcessorProvider {
    return object : SymbolProcessorProvider {
        override fun create(
            environment: SymbolProcessorEnvironment
        ): SymbolProcessor {
            return body(environment)
        }
    }
}