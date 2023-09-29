package com.zml.orm_api;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Orm {
    private static final String DEFAULT_DB_NAME = "zml_orm.db";

    public static final class Builder{
        private Context context;
        private String databaseName = null;

        private Class clazz;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder setClazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public Object build(){
            if (this.context == null){
                throw new NullPointerException("context must not be null");
            }
            if ("".equals(this.databaseName)){
                throw new NullPointerException("the database name  must not be empty");
            }
            if (this.databaseName == null){
                this.databaseName = DEFAULT_DB_NAME;
            }
            String full_name = "com.zml.apt."+this.clazz.getSimpleName()+"Impl";

            try {
                Class<?> clazz = Class.forName(full_name,true,this.clazz.getClassLoader());
                Constructor constructor = clazz.getDeclaredConstructor(Context.class,String.class,int.class);
                return constructor.newInstance(context,databaseName,1);

            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }


    }

}
