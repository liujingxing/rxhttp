package com.rxhttp.compiler

import com.google.devtools.ksp.symbol.KSNode

class KspException(val element: KSNode, msg: String) : Exception(msg)