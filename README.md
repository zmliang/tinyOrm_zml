# tinyOrm_zml
android Room使用过之后，对它的实现原理有了兴趣；也大概知道是怎么做的。
刚好最近也想把android现在比较热门的lifeciycle，databinding，room这些手动简单实现一遍。

这里主要使用apt+javapoet。
其实逻辑很简单：跟我们之前使用sqlite一样，自定义类继承SQLiteOpenHelper；
自定义几个注解:
MyDao: 注解到数据库表操作接口
MyEntity：注解到表结构数据类
MyDataBase:注解到数据库管理抽象类

注解的处理+实现类的代码生成都是通过apt+javapoet；

可能比较难的是这俩框架的api使用

使用方法跟平常使用room的方式一样。

这样基本实现了 增，删，改，查功能。
## 现在有的功能
# 支持自定义sql语句操作
# 多表
# 自定义主键
# sql的增删改查操作


当然这是很基本的，只是用来练手的一个，后面有时候的话也会慢慢优化。

## 后面考虑增加的功能：
# 数据库事务
# 数据库操作线程选择
# 数据库升级策略选择
# 更丰富，灵活的注解

## 使用方式：
新建orm管理类，使用MyDataBase注解，
其他的表结构的创建，操作接口interface的定义略过，

#
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
#

要使用的时候：

               ITestTableEntityDao tableEntityDao = ZMLOrm.getInstance().getTestTableEntityDao();
                TestTableEntity entity = new TestTableEntity();
                entity.age = 10;
                entity.avatar = "头像";
                entity.classRoom = "幼儿园-小班";
                entity.name = "张雨薇";
                tableEntityDao.insert(entity);


                entity.avatar = "头像";
                entity.classRoom = "幼儿园-小班";
                entity.name = "张明亮";
                tableEntityDao.insert(entity);

