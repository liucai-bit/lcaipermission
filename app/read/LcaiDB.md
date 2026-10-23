这是一个基于注解和反射构建的轻量级 SQLite ORM 框架（`lcaidb`）。为了帮助业务方和后续维护者快速上手，我为你编写了以下标准的使用文档。

---

# 📚 Lcaidb 轻量级 ORM 框架使用文档

## 1. 简介
`Lcaidb` 是一个零第三方依赖、纯反射实现的 Android 端 SQLite 数据库 ORM 框架。它支持面向对象的方式操作数据库，**自动建表**、**自动映射 Cursor 到实体**，并通过安全的转义机制防御 SQL 注入。同时兼容了字段的驼峰/下划线自动转换。

## 2. 快速接入

### 2.1 初始化
在 `Application` 或模块初始化阶段，调用 `LcaidbHelper` 进行单例初始化：
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化数据库，传入上下文和数据库名称
        LcaidbHelper.init(this, "my_app.db");
    }
}
```

## 3. 定义数据表实体

新建一个类，继承 `LcaiTableInfo`，并添加 `@TableName` 和 `@TableId` 注解（注解作用于类上）：

```java
import com.liucai.lcaidb.base.module.LcaiTableInfo;
import com.liucai.core.util.lcaidb.TableName;
import com.liucai.core.util.lcaidb.TableId;

@TableName("user_info")   // 指定表名
@TableId("id")            // 指定主键列名（如果是 user_id，这里就写 user_id）
public class User extends LcaiTableInfo {
    
    private Long id;           // 映射为 id 列
    private String userName;   // 自动映射为 user_name 列
    private Integer age;
    private Boolean isVip;     // 自动映射为 INTEGER (1=true, 0=false)

    // 必须有公有无参构造函数
    public User() {}

    // 省略 Getter / Setter ...
}
```
> **说明**：
> 1. 默认情况下，Java 属性 `userName` 会自动映射为数据库列 `user_name`。
> 2. 静态字段(`static`)和瞬态字段(`transient`)会被框架自动忽略，不参与建表和 CRUD。
> 3. 实体及其父类（除 `Object` 外）的字段都会被映射。

---

## 4. 核心 API 使用指南

### 4.1 获取操作门面
所有的 CRUD 操作都通过 `LcaidbCore` 操作类进行：
```java
// 创建 User 对应的数据库操作实例
LcaidbCore<User> userDao = new LcaidbCore<>(User.class);
```

### 4.2 建表
框架支持根据实体类自动建表。如果表已存在，会自动跳过（已通过 `static` 缓存优化，不会每次都查表）：
```java
userDao.createTableForClass();
```

### 4.3 插入数据 (Insert)
```java
// 1. 插入单条数据
User user = new User();
user.setUserName("张三");
user.setAge(25);
user.setIsVip(true);
boolean isSuccess = userDao.insert(user);

// 2. 批量插入数据 (开启事务，提升性能)
List<User> userList = new ArrayList<>();
// ... 组装数据
boolean isBatchSuccess = userDao.batchInsert(userList);
```

### 4.4 删除数据 (Delete)
```java
// 1. 按主键删除单条或多条
List<Serializable> ids = Arrays.asList(1L, 2L, 3L);
boolean delByIds = userDao.deleteById(ids);

// 2. 按条件删除 (支持多个条件，AND 连接)
Map<String, Object> condition = new HashMap<>();
condition.put("age", 18);  // age = 18
condition.put("isVip", true); // is_vip = 1
boolean delByParams = userDao.deleteByParams(condition);

// 3. 按实体非空属性删除 (根据传入实体的非空字段拼接 WHERE 条件)
User delCondition = new User();
delCondition.setUserName("李四");
boolean delByEntity = userDao.deleteByEntity(delCondition);
```

### 4.5 更新数据 (Update)
```java
// 1. 按主键更新
// 必须设置实体中主键(@TableId)的值，非空的其他字段将被更新 (即 SET name=?, age=?)
user.setId(1L);
user.setUserName("张三丰"); // 修改名字
user.setAge(100);         // 修改年龄
boolean upById = userDao.updateById(user);

// 2. 按条件更新 (适合将符合某些条件的某列统一修改)
Map<String, Object> condition = new HashMap<>();
condition.put("age", 18); // WHERE age = 18

Map<String, Object> updateValues = new HashMap<>();
updateValues.put("isVip", true); // SET is_vip = 1
boolean upByParams = userDao.updateByParams(condition, updateValues);
```

### 4.6 查询数据 (Query)
```java
// 1. 查询全表
List<User> allUsers = userDao.query();

// 2. 按主键查询单条 (实体的主键属性必须有值)
User queryEntity = new User();
queryEntity.setId(1L);
User dbUser = userDao.queryById(queryEntity);

// 3. 条件查询
Map<String, Object> queryMap = new HashMap<>();
queryMap.put("age", 25);
List<User> list = userDao.queryByParams(queryMap);

// 4. 统计数据量
long count = userDao.queryCount();
```

### 4.7 分页查询 (Pagination)
分页 `current` 从 **1** 开始计算。
```java
long pageSize = 10;  // 每页10条
long currentPage = 1; // 第1页
String orderBy = "age"; // 排序字段 (数据库列名)
OrderTypeEnum orderType = OrderTypeEnum.DESC; // 降序

// 1. 全表分页
List<User> pageData = userDao.queryPage(pageSize, currentPage, orderBy, orderType);

// 2. 带条件分页
Map<String, Object> condition = new HashMap<>();
condition.put("isVip", true); // 只查 VIP
List<User> vipPageData = userDao.queryPageByParams(pageSize, currentPage, orderBy, orderType, condition);
```

---

## 5. 高级与进阶用法

### 5.1 原生数据库句柄获取
遇到极其复杂的联表查询或聚合查询，你可以直接通过 DAO 获取 `SQLiteDatabase`：
```java
SQLiteDatabase readDb = userDao.getReadSqliteDatabase(); // 读库
SQLiteDatabase writeDb = userDao.getWriteSqliteDatabase(); // 写库（支持读写）
```

### 5.2 支持自定义 SQL 建表
如果自动建表不能满足需求（例如自定义默认值、联合唯一索引），你可以手动执行建表语句：
```java
String customSql = "CREATE TABLE IF NOT EXISTS user_info (id INTEGER PRIMARY KEY AUTOINCREMENT, user_name TEXT UNIQUE)";
userDao.createTable(customSql);
```

### 5.3 安全机制说明 (开发者必读)
1. **防注入处理**：在 `BaseCRUD.wrap()` 中，框架对所有 `String` 类型的值自动进行了 `'` 转义处理，无需担心 `WHERE name = 'O'Reilly'` 导致 SQL 语法崩溃。
2. **空值过滤**：在 `queryByParams` / `updateByParams` / `deleteByParams` 中，如果 `Map` 里面的 `value` 是 `null` 或空字符串 `""`，该条件会被忽略。
3. **布尔类型转换**：Java 中的 `Boolean` 会被映射为 SQLite 的 `INTEGER`，内部存入 `1` 或 `0`，查询时自动还原为 `true` / `false`。
4. **多线程支持**：`SQLiteDatabase` 原生支持多线程并发。我们的 `LcaidbService` 也是无状态设计，表是否存在这个判断也使用了线程安全的静态缓存 `Collections.synchronizedSet`，完全支持在子线程进行数据库操作。

## 6. 注意事项
* **继承体系**：所有的实体类必须继承 `LcaiTableInfo`。
* **构造函数**：实体类**必须**有一个无参的 `public` 构造函数，底层依靠反射实例化对象。
* **字段命名**：强烈建议实体类属性使用**小驼峰**（如 `createTime`），框架会自动将其转换为数据库列名 `create_time`。
* **混淆配置（Proguard）**：如果开启了代码混淆，请务必在 `proguard-rules.pro` 中保留实体类的信息，否则反射会失效导致查不到数据：
  ```proguard
  # 保留所有继承自 LcaiTableInfo 的类及其属性
  -keep class * extends com.liucai.lcaidb.base.module.LcaiTableInfo { *; }
  # 保留注解
  -keepattributes *Annotation*
  ```