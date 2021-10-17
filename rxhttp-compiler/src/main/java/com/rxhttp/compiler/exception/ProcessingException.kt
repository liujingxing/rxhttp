package com.rxhttp.compiler.exception

import javax.lang.model.element.Element

class ProcessingException(val element: Element, msg: String) : Exception(msg)