package com.http.compiler.exception;

import javax.lang.model.element.Element;

public class ProcessingException extends Exception{
    private Element element;

    public ProcessingException(Element element, String msg, Object... args) {
        super(String.format(msg, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
