package com.zml.skin.orm;

import com.zml.anno.MyDataBase;
import com.zml.orm_api.Orm;
import com.zml.skin.App;


@MyDataBase
public abstract class ZMLOrm {

     public abstract ITestDao getTestDao();


     public abstract ITestTableEntityDao getTestTableEntityDao();


     private static final class Holder{
          static final ZMLOrm INSTANCE = (ZMLOrm) new Orm.Builder()
                  .setContext(App.INSTANCE)
                  .setClazz(ZMLOrm.class)
                  .build();
     }

     public final static ZMLOrm getInstance(){
          return Holder.INSTANCE;
     }
}
