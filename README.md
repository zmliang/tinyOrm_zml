# tinyOrm_zml
android Room使用过之后，对它的实现原理有了兴趣；也大概知道是怎么做的。
刚好最近也想把android现在比较热门的lifeciycle，databinding，room这些手动简单实现一遍。

这里主要使用apt+javapoet。
其实逻辑很简单：跟我们之前使用使用sqlite一样，自定义类继承SQLiteOpenHelper；
自定义几个注解:
MyDao: 注解到数据库表操作接口
MyEntity：注解到表结构数据类
MyDataBase:注解到数据库管理抽象类

注解的处理+实现类的代码生成都是通过apt+javapoet；

可能比较难的是这俩框架的api使用

使用方法跟平常使用room的方式一样。

这样基本实现了 增，删，改，查功能。

当然这是很基本的，这是用来练手的一个东西。
