package com.zml.compiler;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.zml.anno.DaoFunction;
import com.zml.anno.MyDao;
import com.zml.anno.MyDataBase;
import com.zml.anno.MyEntity;
import com.zml.anno.SQLEnum;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


/**
 *
 *
 * element是代表程序的一个元素
 * ，这个元素可以是：包、类/接口、属性变量、方法/方法形参、泛型参数
 *
 *
 getEnclosedElements()  返回该元素直接包含的子元素
 getEnclosingElement()   返回包含该element的父element;与上一个方法相反
 getKind()     返回element的类型，判断是哪种element
 getModifiers()     获取修饰关键字,入public static final等关键字
 getSimpleName()    获取名字，不带包名
 getQualifiedName()  获取全名
 getParameters()         获取方法的参数元素
 getReturnType()        获取方法的返回值
 getConstantValue()

 */

@SupportedAnnotationTypes({
        "com.zml.anno.MyDao",
        "com.zml.anno.MyEntity",
        "com.zml.anno.DaoFunction"
})
@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {

    private static final String DB_HELPER_IMPL_CLASS_NAME = "_DBHelper";

    private static final String BASE_PACKAGE_NAME = "com.zml.apt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSS");


    Elements elementUtils;
    Types typeUtils;
    Messager messager;
    Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // Element操作类，用来处理Element的工具
        elementUtils = processingEnv.getElementUtils();

        // 类信息工具类，用来处理TypeMirror的工具
        typeUtils = processingEnv.getTypeUtils();

        // 日志工具类，因为在process()中不能抛出一个异常，那会使运行注解处理器的JVM崩溃。所以Messager提供给注解处理器一个报告错误、警告以及提示信息的途径，用来写一些信息给使用此注解器的第三方开发者看
        messager = processingEnv.getMessager();

        // 文件工具类，常用来读取或者写资源文件
        filer = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.size()<=0){
            return false;
        }

        MyEntityParser myEntityParser = tableAnnotation(roundEnvironment.getElementsAnnotatedWith(MyEntity.class));

        Map<String,TypeName> daoImplMap = daoAnnotation(roundEnvironment.getElementsAnnotatedWith(MyDao.class));

        TypeSpec dbHelperType = buildDbHelper(myEntityParser.targetElementMap);

        databaseAnimation(roundEnvironment.getElementsAnnotatedWith(MyDataBase.class),dbHelperType,daoImplMap);

        return true;
    }

    private MyEntityParser tableAnnotation(Set<? extends Element> elements){

        MyEntityParser myEntityParser = new MyEntityParser();
        for (Element element:elements){
            if (element.getKind()!=ElementKind.CLASS){
                throw new RuntimeException("MyEntity annotation error");
            }
            MyEntity myEntity = element.getAnnotation(MyEntity.class);
            if (myEntity == null){
                continue;
            }
            String table_name = myEntity.table_name();

            myEntityParser.targetElementMap.put(table_name,element);
            myEntityParser.targetTypeNameMap.put(table_name,ClassName.get(element.asType()));
        }

        return myEntityParser;
    }

    /**
     * MyDataBase注解
     * @param elements
     */
    private void databaseAnimation(Set<? extends Element> elements,TypeSpec dbHelperType,Map<String,TypeName> daoTypeMap){
        if (elements.size()>1){
            throw new RuntimeException("can't annotation MyDataBase in mulitily class");
        }
        for (Element element:elements){
            String _name = element.getSimpleName().toString();//被注解的class 名称

            TypeSpec.Builder _typeBuilder = TypeSpec.classBuilder(_name+"Impl")//接口实现类
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .superclass(ClassName.bestGuess(element.asType().toString()));
//
            List<? extends Element> methods = element.getEnclosedElements();//接口中所有方法 method

            for (Element e:methods){
//
                String method_name = e.getSimpleName().toString();
                if (e.getKind()!=ElementKind.METHOD){
                    continue;
                }
                if (!e.getModifiers().contains(Modifier.ABSTRACT)){
                    continue;
                }

                TypeName returnType = TypeName.get(((ExecutableElement)e).getReturnType());


                //生成方法
                MethodSpec.Builder _mBuilder =  MethodSpec.methodBuilder(method_name)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ClassName.get(Override.class))
                        .returns(returnType);

                List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();//方法所有参数
                for (VariableElement p:params){
                    _mBuilder.addParameter(ClassName.get(p.asType()),p.toString());
                }
                if (!TypeName.VOID.equals(returnType)){
                    if (daoTypeMap.containsValue(returnType)){
                        String simpleName = "m"+elementUtils.getTypeElement(returnType.toString()).getSimpleName().toString();
                        _mBuilder.addStatement("return this.$L",simpleName);
                    }else {
                        _mBuilder.addStatement("return null");
                    }
                }

                MethodSpec methodSpec = _mBuilder.build();

                _typeBuilder.addMethod(methodSpec);

            }


            //构造方法
            MethodSpec.Builder constructBuilder = MethodSpec.constructorBuilder()
                    .addParameter(ClassName.bestGuess("android.content.Context"),"context")
                    .addParameter(ClassName.get(String.class),"dbName")
                    .addParameter(int.class,"version")
                    .addStatement("this.dbHelper = new $L(context,dbName,version)",DB_HELPER_IMPL_CLASS_NAME);

            for (Map.Entry entry:daoTypeMap.entrySet()){
                String fieldName = "m"+entry.getKey();
                String fieldType = entry.getKey().toString()+"Impl";
                constructBuilder.addStatement("this.$L = new $L(this.dbHelper.getWritableDatabase())",fieldName,fieldType);
                _typeBuilder.addField(FieldSpec.builder((TypeName) entry.getValue(),fieldName,Modifier.PRIVATE,Modifier.FINAL)
                        .build());
            }

            _typeBuilder.addMethod(constructBuilder
                    .build());

            //构造dbhelper字段
            _typeBuilder.addField(FieldSpec.builder(dbHelperType.superclass,"dbHelper",Modifier.PRIVATE,Modifier.FINAL)
                    .build());




            JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_NAME, _typeBuilder.build())//写入文件
                    .addFileComment("GENERATED CODE BY ZMLIANG. DO NOT NEED MODIFY! $S",
                            DATE_FORMAT.format(new Date(System.currentTimeMillis())))
                    .skipJavaLangImports(true)
                    .build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
    private Map<String,TypeName> daoAnnotation(Set<? extends Element> elements){

        Map<String,TypeName> result = new HashMap<>();
        for (Element element:elements){

            if (element.getKind() == ElementKind.INTERFACE){
                String _name = element.getSimpleName().toString();//被注解的接口名

                TypeName interfaceTypeName = ClassName.bestGuess(element.asType().toString());
                TypeSpec.Builder _typeBuilder = TypeSpec.classBuilder(_name+"Impl")//接口实现类
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(interfaceTypeName);

                result.put(_name,interfaceTypeName);

                List<? extends Element> child = element.getEnclosedElements();//接口中所有方法 method
                for (Element e:child){

                    String method_name = e.getSimpleName().toString();
                    DaoFunction daoFunction = e.getAnnotation(DaoFunction.class);
                    if (daoFunction == null){
                        continue;
                    }

                    String customSql = daoFunction.sql();
                    SQLEnum operEnum = daoFunction.sqlEnum();


                    TypeName returnType = TypeName.get(((ExecutableElement)e).getReturnType());

                    boolean haveReturn = !TypeName.VOID.equals(returnType);
                    //构造方法
                   MethodSpec.Builder _mBuilder =  MethodSpec.methodBuilder(method_name)
                           .addModifiers(Modifier.PUBLIC)
                           .addAnnotation(ClassName.get(Override.class))
                           .returns(returnType);

                    List<? extends VariableElement> params = ((ExecutableElement)e).getParameters();//方法所有参数
                    String table_name = "";
                    List<? extends Element> allFields = null;
                    for (VariableElement p:params){
                        TypeElement typeElement = elementUtils.getTypeElement(ClassName.get(p.asType()).toString());

                        if (typeElement!=null){
                            allFields = typeElement.getEnclosedElements();
                            MyEntity myEntity = typeElement.getAnnotation(MyEntity.class);

                            if (myEntity!=null){
                                table_name = myEntity.table_name();
                            }
                        }

                        _mBuilder.addParameter(ClassName.get(p.asType()),p.toString());
                    }

                    switch (operEnum){
                        case INSERT:
                        {
                            StringBuilder sb = new StringBuilder("INSERT OR REPLACE INTO ");
                            sb.append("`").append(table_name).append("` ").append("(");

                            StringBuilder sb1 = new StringBuilder(" VALUES (");

                            StringBuilder values = new StringBuilder();
                            String paramName = params.get(0).toString();

                            if (allFields != null){
                                int size = allFields.size();
                                for (int i=0;i<size;i++){
                                    Element field = allFields.get(i);
                                    if (field.getKind()!=ElementKind.FIELD){
                                        continue;
                                    }
                                    String last = (i == size-1 ? "" : ",");
                                    values.append(paramName).append(".").append(field).append(last);
                                    sb.append("`").append(field).append("`").append(last);
                                    sb1.append("?").append( last);
                                }
                            }
                            if (values.charAt(values.length()-1) == ','){
                                values.deleteCharAt(values.length()-1);
                            }
                            if (sb.charAt(sb.length()-1) == ','){
                                sb.deleteCharAt(sb.length()-1);
                            }
                            if (sb1.charAt(sb1.length()-1) == ','){
                                sb1.deleteCharAt(sb1.length()-1);
                            }

                            sb.append(")");
                            sb1.append(")");

                            //statement
                            String sql = sb.append(sb1).toString();
                            _mBuilder.addStatement("this.mDataBase.execSQL(\"$L\",new Object[] { $L })",sql,values.toString());

                        }
                            break;
                        case DELETE:
                        {
                            StringBuilder sqlSb = new StringBuilder((customSql == null || "".equals(customSql)) ? "DELETE FROM " : customSql);
                            StringBuilder values = new StringBuilder();
                            if (params.size()>0)
                            {
                                if (table_name != null && !"".equals(table_name)){
                                    sqlSb.append("`")
                                            .append(table_name)
                                            .append("`")
                                            .append(" WHERE");
                                }else {
                                    sqlSb.append(" WHERE");
                                }
                            }

                            for (int i=0;i< params.size();i++){
                                boolean last = i == params.size()-1;
                                VariableElement p = params.get(i);
                                TypeElement typeElement = elementUtils.getTypeElement(ClassName.get(p.asType()).toString());

                                if (typeElement!=null && !"java.lang.String".equals(typeElement.toString())){

                                    allFields = typeElement.getEnclosedElements();

                                    for (int j=0;j< allFields.size();j++){
                                        last = (j == allFields.size()-1);
                                        Element field = allFields.get(j);
                                        if (field.getKind()!=ElementKind.FIELD){
                                            continue;
                                        }
                                        values.append(p)
                                                .append(".")
                                                .append(field).append(last ? "" : ",");
                                        sqlSb.append(" `")
                                                .append(field)
                                                .append("`")
                                                .append(" = ?")
                                                .append(last ? "" : " AND");
                                    }
                                    break;

                                }else {//参数是基本数据类型
                                    values.append(p).append(last ? "" : ",");
                                    sqlSb.append(" `")
                                            .append(p)
                                            .append("`")
                                            .append(" = ?")
                                            .append(last ? "" : " AND");
                                }
                            }
                            _mBuilder.addStatement("this.mDataBase.execSQL(\"$L\",new Object[] { $L })",sqlSb.toString(),values.toString());

                        }
                            break;
                        case UPDATE:
                        {
                            StringBuilder sql = new StringBuilder((customSql == null || "".equals(customSql)) ? "UPDATE OR ABORT " : customSql);
                            StringBuilder values = new StringBuilder();
                            if (params.size()>0 && sql.indexOf(":") == -1) {
                                if (table_name != null && !"".equals(table_name)){
                                    sql.append("`")
                                            .append(table_name)
                                            .append("`")
                                            .append(" SET");
                                }else {
                                    sql.append(" SET");
                                }
                            }
                           
                            Map<Integer,String> map = new TreeMap<>();
                            for (int i=0;i< params.size();i++){
                                VariableElement p = params.get(i);
                                TypeElement typeElement = elementUtils.getTypeElement(ClassName.get(p.asType()).toString());

                                if (typeElement!=null && !"java.lang.String".equals(typeElement.toString())){

                                    allFields = typeElement.getEnclosedElements();

                                    for (int j=0;j< allFields.size();j++){
                                        boolean last = (j == allFields.size()-1);
                                        Element field = allFields.get(j);
                                        if (field.getKind()!=ElementKind.FIELD){
                                            continue;
                                        }
                                        values.append(p)
                                                .append(".")
                                                .append(field).append(last ? "" : ",");
                                        sql.append(" `")
                                                .append(field)
                                                .append("`")
                                                .append(" = ?")
                                                .append(last ? "" : " ,");
                                    }
                                    break;
                                } else {//参数是基本数据类型
                                    String pName = p.toString();
                                    int start = sql.indexOf(":"+pName);
                                    if (start == -1){
                                        continue;
                                    }
                                    map.put(start,pName);
                                    sql.replace(start,start+1+pName.length(),"?");
                                }
                            }
                            int index = 0;
                            int size = map.size();
                            for (String para:map.values()){
                                boolean last = index == size-1;
                                values.append(para)
                                        .append(last ? "" : ",");
                                index++;
                            }

                            _mBuilder.addStatement("this.mDataBase.execSQL(\"$L\",new Object[] { $L })",sql.toString(),values.toString());

                        }
                            break;
                        case QUERY:
                        {


                            StringBuilder indexSb = new StringBuilder();
                            StringBuilder valOfSb = new StringBuilder();
                            StringBuilder newItemSb = new StringBuilder();
                            String addItemStr = "\n";
                            if (haveReturn){

                                TypeElement ret = elementUtils.getTypeElement(returnType.toString()) ;

                                if (returnType.getClass() == ParameterizedTypeName.class){
                                    TypeName typeArg = ((ParameterizedTypeName)returnType).typeArguments.get(0);
                                    TypeName _tn =  ParameterizedTypeName.get(ClassName.get(ArrayList.class),typeArg);
                                    _mBuilder.addStatement("$L result = new $L()",returnType,_tn);
                                    ret = elementUtils.getTypeElement(typeArg.toString());
                                    addItemStr = ("result.add(_resultOfItem);\n");
                                }else {
                                    _mBuilder.addStatement("$L result = new $L()",ret,ret);
                                }
                                TypeElement tab_type_ele = elementUtils.getTypeElement(ClassName.get(ret.asType()).toString());
                                if (tab_type_ele!=null){
                                    MyEntity myEntity = tab_type_ele.getAnnotation(MyEntity.class);
                                    if (myEntity!=null){
                                        table_name = myEntity.table_name();
                                    }
                                }


                                newItemSb.append(ret)
                                        .append( " _resultOfItem = new ")
                                        .append(ret).append("()").append(";\n");
                                for (Element retField:ret.getEnclosedElements()){
                                    if (retField.getKind()!=ElementKind.FIELD){
                                        continue;
                                    }
                                    indexSb.append("int _indexOf").append(retField).append(" = c.getColumnIndexOrThrow(").append("\""+retField+"\"").append(");\n");


                                    valOfSb.append("if(_indexOf").append(retField).append(" != -1)")
                                            .append("{\n")
                                            .append(" _resultOfItem.")
                                            .append(retField).append(" = ")
                                            .append("c.").append(Utils.getFunction(retField.asType())).append("(")
                                            .append("_indexOf").append(retField).append(")").append(";\n")
                                            .append("}\n");
                                }
                            }


                            StringBuilder sqlSb = new StringBuilder();
                            StringBuilder paramSb = new StringBuilder();
                            sqlSb.append("SELECT * FROM ")
                                    .append("`")
                                    .append(table_name)
                                    .append("`");
                            if (params.size()>0){
                                sqlSb.append(" WHERE ");
                            }
                            for (int i=0;i< params.size();i++){
                                boolean last = i == params.size()-1;
                                VariableElement p = params.get(i);
                                sqlSb.append("`")
                                        .append(p)
                                        .append("`")
                                        .append("=?")
                                        .append(last ? "" : " AND ");
                                paramSb.append("String.valueOf(")
                                        .append(p)
                                        .append(")")
                                        .append(last ? "" : ",");
                            }

                            if (customSql!=null && !customSql.trim().equals("")){
                                sqlSb = new StringBuilder(customSql);
                            }
                            _mBuilder.addStatement("$L c = this.mDataBase.rawQuery(\"$L\",new String[] { $L })",
                                    ClassName.bestGuess("android.database.Cursor"),sqlSb.toString(),paramSb.toString())
                                    .addStatement(CodeBlock.builder()
                                            .add(indexSb.toString())
                                            .build())
                                    .addStatement(CodeBlock.builder()
                                            .add("while (c.moveToNext()) {\n")
                                            .add(newItemSb.toString())
                                            .add(valOfSb.toString())
                                            .add(addItemStr)
                                            .add("\n}")
                                            .build())
                                    .addStatement("c.close()");

                            if (haveReturn){
                                _mBuilder.addStatement("return result");
                            }

                        }
                            break;
                        default:
                            break;
                    }
                    
                    MethodSpec methodSpec = _mBuilder.build();

                    _typeBuilder.addMethod(methodSpec);

                }
                _typeBuilder.addField(FieldSpec.builder(TypeName.get(String.class),"table_name",Modifier.PRIVATE).build());
                _typeBuilder.addField(FieldSpec.builder(ClassName.bestGuess("android.database.sqlite.SQLiteDatabase"),
                        "mDataBase",Modifier.PRIVATE,Modifier.FINAL).build());

                _typeBuilder.addMethod(MethodSpec.constructorBuilder()
                                .addParameter(ClassName.bestGuess("android.database.sqlite.SQLiteDatabase"),"dataBase")
                                .addStatement("this.mDataBase=dataBase")
                        .build());

                TypeSpec typeSpec = _typeBuilder.build();

                JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_NAME, typeSpec)//写入文件
                        .addFileComment("GENERATED CODE BY ZMLIANG. DO NOT NEED MODIFY! $S",
                                DATE_FORMAT.format(new Date(System.currentTimeMillis())))
                        .skipJavaLangImports(true)
                        .build();

                try {
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();

                }

            }
        }
        return result;
    }

    private TypeSpec buildDbHelper(Map<String,Element> map){
        MethodSpec.Builder _mb = MethodSpec.methodBuilder("onCreate")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(ClassName.get(Override.class))
                .addParameter(ClassName.bestGuess("android.database.sqlite.SQLiteDatabase"),"db");

        for (Map.Entry entry: map.entrySet()){
            String table_name = entry.getKey().toString();
            Element element = (Element) entry.getValue();
            MyEntity myEntity = element.getAnnotation(MyEntity.class);
            String primaryKey  = "";
            if (myEntity!=null){
                primaryKey = myEntity.primary_key();
            }

            List<? extends Element> fields = element.getEnclosedElements();
            StringBuilder sb = new StringBuilder("(");
            for (int i=0;i<fields.size();i++){
                Element field = fields.get(i);
                if (field.getKind()!=ElementKind.FIELD){
                    continue;
                }
                boolean isPrimaryKey = primaryKey.equals(field.toString());
                String sqlType = Utils.getSQLType(TypeName.get(field.asType()).toString());
                sb.append("`")
                        .append(field)
                        .append("`")
                        .append(" ")
                        .append(sqlType)
                        .append(isPrimaryKey ? " PRIMARY KEY" +(sqlType.equals("INTEGER") ? " AUTOINCREMENT" : "") : "")
                        .append(",");
            }

            if (sb.charAt(sb.length()-1) == ','){
                sb = sb.deleteCharAt(sb.length()-1);
            }
            sb.append(");");
            _mb.addStatement("db.execSQL(\"CREATE TABLE IF NOT EXISTS `$L` $L\")",table_name,sb.toString());

        }

        MethodSpec _create = _mb.build();

        MethodSpec _upgrade = MethodSpec.methodBuilder("onUpgrade")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(ClassName.get(Override.class))
                .addParameter(ClassName.bestGuess("android.database.sqlite.SQLiteDatabase"),"db")
                .addParameter(int.class,"oldVersion")
                .addParameter(int.class,"newVersion")
                .build();



        MethodSpec _construct = MethodSpec.constructorBuilder()
                .addParameter(ClassName.bestGuess("android.content.Context"),"context")
                .addParameter(ClassName.get(String.class),"dbName")
                .addParameter(int.class,"version")
                .addStatement("super(context,dbName,null,version)")
                .build();




        TypeSpec clazz = TypeSpec.classBuilder(DB_HELPER_IMPL_CLASS_NAME)//创建类
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(_construct)
                .addMethod(_create)
                .addMethod(_upgrade)
                .superclass(ClassName.bestGuess("android.database.sqlite.SQLiteOpenHelper"))
                .build();


        JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_NAME, clazz)//写入文件
                .addFileComment("GENERATED CODE BY ZMLIANG. DO NOT NEED MODIFY! $S",
                        DATE_FORMAT.format(new Date(System.currentTimeMillis())))
                .skipJavaLangImports(true)
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clazz;
    }



    public void assetSqlType(String customSql,SQLEnum sqlEnum){

        switch (sqlEnum){
            case QUERY:
                break;
            case DELETE:
                break;
            case INSERT:
                break;
            case UPDATE:
                break;
        }
    }
}