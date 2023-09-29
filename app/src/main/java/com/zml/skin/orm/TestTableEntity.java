package com.zml.skin.orm;


import com.zml.anno.MyEntity;

@MyEntity(table_name = "test_table",primary_key = "id")
public class TestTableEntity {
    public int id;
    public String name;
    public String avatar;
    public int age;
    public String classRoom;

    @Override
    public String toString() {
        return "TestTableEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", age=" + age +
                ", classRoom='" + classRoom + '\'' +
                '}';
    }
}
