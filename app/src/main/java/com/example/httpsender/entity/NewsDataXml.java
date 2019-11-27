package com.example.httpsender.entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ljx
 * Date: 2019-11-22
 * Time: 23:34
 */
@Root(name = "body", strict = false) //name:要解析的xml数据的头部
public class NewsDataXml {
    @Attribute
    public String copyright; //属性
    @ElementList(required = true, inline = true, entry = "route") //标志是集合
    public List<NewsXml> newsXmls = new ArrayList<>();
}

