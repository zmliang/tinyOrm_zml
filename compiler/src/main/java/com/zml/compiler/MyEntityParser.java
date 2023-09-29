package com.zml.compiler;

import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;

class MyEntityParser {

     public Map<String, Element> targetElementMap = new HashMap<>();

     public Map<String, TypeName> targetTypeNameMap = new HashMap<>();

}
