package com.zml.skin.orm;


import com.zml.anno.DaoFunction;
import com.zml.anno.MyDao;
import com.zml.anno.SQLEnum;

import java.util.List;

@MyDao
public interface ITestDao {

    @DaoFunction(sqlEnum = SQLEnum.INSERT,sql = "insert table ")
    void insert(SecondEntity secondEntity);

    @DaoFunction(sqlEnum = SQLEnum.DELETE)
    void delete(SecondEntity secondEntity);

    @DaoFunction(sqlEnum = SQLEnum.QUERY)
    SecondEntity query(int id,String value);


    @DaoFunction(sqlEnum = SQLEnum.QUERY)
    List<SecondEntity> queryBy(int id, String value);


}
