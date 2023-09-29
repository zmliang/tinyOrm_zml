package com.zml.compiler;

import javax.lang.model.type.TypeMirror;

public class Utils {

    public static String getSQLType(String javaType){
        switch (javaType){
            case "java.lang.String":
                return "TEXT";
            case "int":
            case "java.lang.Integer":
                return "INTEGER";

            case "long":
            case "java.lang.Long":
                return "BIGINT";

            case "float":
            case "java.lang.Float":
                return "FLOAT";

            case "boolean":
            case "java.lang.Boolean":
                return "CHAR(1)";

            case "byte":
            case "java.lang.Byte":
                return "TINYINT";

            case "java.util.Calendar":
            case "java.util.Date":
                return "TIMESTAMP";

        }

        return "INTEGER";
    }

    public static String getFunction(TypeMirror typeMirror){
        switch (typeMirror.toString()){
            case "java.lang.String":
                return "getString";
            case "int":
            case "java.lang.Integer":
                return "getInt";

            case "short":
            case "java.lang.Short":
                return "getShort";

            case "long":
            case "java.lang.Long":
                return "getLong";

            case "float":
            case "java.lang.Float":
                return "getFloat";

            case "double":
            case "java.lang.Double":
                return "getDouble";

            case "class [B":
                return "getBlob";

            default:
                break;
        }

        return "";
    }


    public static String initValue(TypeMirror typeMirror){
        switch (typeMirror.toString()){
            case "int":
            case "long":
            case "float":
            case "double":
                return "0";
            default:
                break;
        }
        return "null";
    }

}
