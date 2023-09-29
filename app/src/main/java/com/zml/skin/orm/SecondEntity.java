package com.zml.skin.orm;


import com.zml.anno.MyEntity;

@MyEntity(table_name = "second_table",primary_key = "id")
public class SecondEntity {
    public int id;

    public String value;


}
