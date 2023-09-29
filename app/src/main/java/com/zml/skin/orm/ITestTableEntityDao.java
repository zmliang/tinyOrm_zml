package com.zml.skin.orm;

import com.zml.anno.DaoFunction;
import com.zml.anno.MyDao;
import com.zml.anno.SQLEnum;

import java.util.List;

@MyDao
public interface ITestTableEntityDao {

    @DaoFunction(sqlEnum = SQLEnum.QUERY)
    List<TestTableEntity> queryAll();

    @DaoFunction(sqlEnum = SQLEnum.INSERT)
    void insert(TestTableEntity testTableEntity);

    @DaoFunction(sqlEnum = SQLEnum.DELETE,sql = "DELETE FROM `test_table`")
    void deleteBy(int id,String avatar);


    @DaoFunction(sqlEnum = SQLEnum.UPDATE,sql = "UPDATE OR ABORT `test_table` SET `id`=:id,`name`=:name WHERE `age` =:age")
    void updateBy(String name,int age,int id);
}
