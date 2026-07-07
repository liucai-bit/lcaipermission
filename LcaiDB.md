LcaiDB - Android SQLite ORM 框架

项目简介

LcaiDB 是一个基于 Android SQLite 数据库的轻量级 ORM（对象关系映射）框架。它提供了简洁的 API 来执行常见的数据库操作，包括表的创建、数据的增删改查（CRUD）、分页查询等，旨在简化 Android 应用中的数据库操作。

核心特性

- 自动建表：根据实体类自动创建数据库表
- 完整的 CRUD 操作：支持插入、删除、更新、查询等基本操作
- 分页查询：内置分页功能，支持排序和条件筛选
- 批量操作：支持批量插入和批量删除
- 线程安全：使用读写锁确保数据库访问的线程安全
- 驼峰命名转换：自动将 Java 的驼峰命名转换为数据库的下划线命名
- 注解支持：通过注解配置表名和主键

📦 集成方式
  添加依赖
  implementation "com.github.liucai-bit:lcaipermission:v1.1.3"

项目结构

核心类说明

1. LcaidbCore (`LcaidbCore.java`)
框架的核心入口类，继承自 `BaseCRUD`，提供了所有数据库操作的公共接口。

主要方法：
- `createTableForClass()` - 根据实体类创建表
- `insert(T entity)` - 插入单条数据
- `batchInsert(List<T> entitys)` - 批量插入数据
- `deleteById(Collection<Serializable> idList)` - 根据ID批量删除
- `deleteByParams(Map<String,Object> condition)` - 根据条件删除
- `deleteByEntity(T entity)` - 根据实体删除
- `updateById(T entity)` - 根据ID更新
- `updateByParams(Map<String,Object> condition, Map<String,Object> update)` - 根据条件更新
- `query()` - 查询所有数据
- `queryById(T entity)` - 根据ID查询
- `queryByParams(Map<String,Object> condition)` - 根据条件查询
- `queryPage()` / `queryPageByParams()` - 分页查询（支持条件）

2. BaseCRUD (`BaseCRUD.java`)
抽象基类，实现了 `LcaiSql` 接口，负责实体类的初始化和注解解析。

主要功能：
- 通过反射实例化实体类
- 获取表名和主键的注解信息
- 提供 SQL 模板的基础支持

3. LcaidbService (`LcaidbService.java`)
服务接口，定义了所有数据库操作的方法契约。

接口方法包括：
- 表的创建和管理
- 数据的增删改查
- 分页查询
- 数据库连接管理

4. LcaidbHelper (`LcaidbHelper.java`)
数据库帮助类，继承自 `SQLiteOpenHelper`，负责数据库的创建、升级和连接管理。

特性：
- 单例模式确保全局唯一实例
- 读写分离：提供独立的可读和可写数据库连接
- 线程安全：使用 `ReentrantReadWriteLock` 控制并发访问
- 性能优化：支持 WAL（Write-Ahead Logging）模式
- 自动资源管理：在 `finalize()` 中自动关闭数据库连接

5. 查询模块 (`Query.java`)
负责构建查询 SQL 语句和结果集映射。

主要功能：
- `getEntity(Cursor cursor, Class<?> aClass)` - 将 Cursor 结果映射到实体对象
- 支持条件查询、分页查询、排序查询
- 自动类型转换（String、Integer、Double、Float、Long、Short）
- 驼峰命名到下划线命名的自动转换

6. 删除模块 (`Delete.java`)
负责构建删除操作的 SQL 语句。

支持三种删除方式：
- 根据ID删除（支持批量）
- 根据条件删除
- 根据实体删除

7. 分页模块
- ILcaiPage (`ILcaiPage.java`) - 分页接口
- LcaiPage (`LcaiPage.java`) - 分页实现类

分页参数：
- `getSize()` - 每页显示条数
- `getCurrent()` - 当前页码
- `getStart()` - 计算起始位置（默认实现）
- `getEnd()` - 计算结束位置（默认实现）

8. 工具类
- LcaiDbResult (`LcaiDbResult.java`) - 数据库操作结果处理
- OrderTypeEnum (`OrderTypeEnum.java`) - 排序枚举（ASC/DESC）

快速开始

1. 初始化数据库

```java
// 在 Application 或主 Activity 中初始化
LcaidbHelper.init(context, "your_database_name.db");
```

2. 定义实体类

实体类需要继承 `LcaiTableInfo` 并使用注解配置表信息：

```java
@TableName("user_table")
public class User extends LcaiTableInfo {
    @TableId
    private Long id;
    
    private String userName;
    private Integer age;
    private String email;
    
    // getters and setters
}
```

3. 使用 LcaidbCore 进行操作

```java
// 创建核心操作对象
LcaidbCore<User> userDao = new LcaidbCore<>(User.class);

// 创建表
userDao.createTableForClass();

// 插入数据
User user = new User();
user.setUserName("张三");
user.setAge(25);
user.setEmail("zhangsan@example.com");
boolean success = userDao.insert(user);

// 查询所有用户
List<User> users = userDao.query();

// 条件查询
Map<String, Object> condition = new HashMap<>();
condition.put("age", 25);
List<User> youngUsers = userDao.queryByParams(condition);

// 分页查询（第2页，每页10条，按ID降序）
List<User> pageUsers = userDao.queryPage(10, 2, "id", OrderTypeEnum.DESC);

// 更新数据
user.setAge(26);
userDao.updateById(user);

// 删除数据
List<Serializable> ids = Arrays.asList(1L, 2L, 3L);
userDao.deleteById(ids);
```

配置说明

数据库配置
- 数据库名称：默认为 "app_database.db"，可通过 `LcaidbHelper.init()` 自定义
- 数据库版本：当前版本为 1
- 页大小：默认 1024
- 日志模式：支持 WAL（Write-Ahead Logging）
- 外键约束：默认启用

性能优化
1. 连接池管理：`LcaidbHelper` 维护了可读和可写数据库连接的单例
2. 线程安全：使用读写锁确保高并发场景下的数据一致性
3. WAL 模式：提高写入性能，支持读写并发
4. 批量操作：提供批量插入接口，减少事务开销

注意事项

1. 实体类要求：
   - 必须继承 `LcaiTableInfo`
   - 必须有无参构造函数
   - 字段类型需在 `Query.getEntity()` 方法支持的类型范围内

2. 命名约定：
   - 数据库表名和列名默认使用下划线命名
   - Java 实体类字段使用驼峰命名，框架会自动转换

3. 线程安全：
   - `LcaidbHelper` 的数据库连接方法是线程安全的
   - 建议在 UI 线程外执行数据库操作

4. 资源管理：
   - 框架会自动管理数据库连接
   - 应用退出时连接会自动关闭

版本信息

- 当前版本：1.0
- 最后更新：2026年6月9日
- 作者：liucai / liucai
- 项目名称：lcpermission

许可证

本项目为开源项目，具体许可证信息请查看项目根目录下的 LICENSE 文件。

---

提示：使用前请确保已添加必要的依赖和权限，并在实际项目中充分测试。