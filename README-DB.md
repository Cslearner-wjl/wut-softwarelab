# 校园二手交易平台数据库初始化说明

本项目提供了两种数据库配置方式：H2内存数据库和MySQL数据库。

## H2内存数据库（开发测试环境）

H2内存数据库配置已经集成到应用程序中，无需额外安装，适合开发和测试环境使用。

启动应用程序时，系统会自动执行以下操作：
1. 使用`schema.sql`创建数据库表结构
2. 使用`data.sql`插入初始测试数据
3. 启动H2控制台，可通过`http://localhost:8080/h2-console`访问

H2控制台连接信息：
- JDBC URL: `jdbc:h2:mem:d_trade`
- 用户名: `sa`
- 密码: 空

## MySQL数据库（生产环境）

### 手动初始化MySQL数据库

1. 安装并启动MySQL数据库服务
2. 使用root账户或其他有权限创建数据库的账户登录MySQL
3. 执行`mysql-init.sql`脚本初始化数据库：

```bash
mysql -u root -p < src/main/resources/mysql-init.sql
```

该脚本会执行以下操作：
- 创建`d_trade`数据库
- 创建`d_trade_user`用户并授予权限
- 创建所有必要的表结构
- 插入初始测试数据

### 配置应用程序使用MySQL

修改`application.yml`文件或使用`application-mysql.yml`配置文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/d_trade?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: d_trade_user
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none
```

如果使用`application-mysql.yml`配置文件，可以通过以下命令启动应用：

```bash
java -jar d_trade.jar --spring.profiles.active=mysql
```

## 数据库表结构说明

### users表（用户表）
- `id`: 用户ID，自增主键
- `student_id`: 学号，唯一标识
- `username`: 用户名
- `password`: 密码（加密存储）
- `nickname`: 昵称
- `contact_info`: 联系信息
- `enabled`: 是否启用
- `role`: 角色（USER/ADMIN）
- `create_time`: 创建时间
- `update_time`: 更新时间

### products表（商品表）
- `id`: 商品ID，自增主键
- `title`: 商品标题
- `description`: 商品描述
- `price`: 商品价格
- `trade_location`: 交易地点
- `image_paths`: 图片路径（JSON数组）
- `status`: 商品状态（AVAILABLE/SOLD/REMOVED）
- `seller_id`: 卖家ID（关联users表）
- `create_time`: 创建时间
- `update_time`: 更新时间

### orders表（订单表）
- `id`: 订单ID，自增主键
- `product_id`: 商品ID（关联products表）
- `buyer_id`: 买家ID（关联users表）
- `seller_id`: 卖家ID（关联users表）
- `status`: 订单状态（PENDING/COMPLETED/CANCELED）
- `create_time`: 创建时间
- `update_time`: 更新时间

### messages表（消息表）
- `id`: 消息ID，自增主键
- `type`: 消息类型
- `title`: 消息标题
- `content`: 消息内容
- `is_read`: 是否已读
- `receiver_id`: 接收者ID（关联users表）
- `product_id`: 相关商品ID（关联products表，可为空）
- `order_id`: 相关订单ID（关联orders表，可为空）
- `create_time`: 创建时间

## 初始测试账户

系统预设了以下测试账户：

1. 管理员账户
   - 学号: `admin`
   - 密码: `admin123`

2. 普通用户账户
   - 学号: `20210101`（张三）
   - 学号: `20210102`（李四）
   - 学号: `20210103`（王五）
   - 密码均为: `password` 

# 数据库配置说明

## 数据库选项

本项目支持两种数据库配置：
1. MySQL（生产环境）
2. H2内存数据库（开发/测试环境）

## MySQL配置

MySQL是生产环境的首选数据库。配置如下：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/d_trade?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

### MySQL初始化步骤

1. 创建MySQL数据库：

```sql
CREATE DATABASE d_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
   - 方法1：通过MySQL客户端执行 `src/main/resources/mysql_init.sql` 文件
   - 方法2：使用命令行

```bash
mysql -u root -p < src/main/resources/mysql_init.sql
```

## H2内存数据库配置（开发/测试）

H2是一个轻量级的内存数据库，适合开发和测试环境使用。配置如下：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:d_trade
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
      path: /h2-console
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      data-locations: classpath:data.sql
```

### 使用H2数据库

1. 启动应用程序时指定profile：

```bash
java -jar your-application.jar --spring.profiles.active=h2
```

2. 或者在开发环境中设置`application.yml`文件：

```yaml
spring:
  profiles:
    active: h2
```

3. 访问H2控制台：
   - 启动应用后，访问 http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:d_trade
   - 用户名: sa
   - 密码: (空)

## 测试数据

系统初始化后包含以下测试数据：

### 用户
- 管理员: 学号 `admin`，密码 `admin123`
- 测试用户1: 学号 `20210101`，密码 `password`，昵称 `三哥`
- 测试用户2: 学号 `20210102`，密码 `password`，昵称 `四哥`
- 测试用户3: 学号 `20210103`，密码 `password`，昵称 `五哥`

### 商品
系统初始化了5个测试商品，包括iPad、书籍、运动鞋等。

### 订单和消息
系统初始化了测试订单和相关的消息数据。

## 表结构

### users表
- 用户信息，包含学号、用户名、密码等

### products表
- 商品信息，包含标题、描述、价格等

### orders表
- 订单信息，关联买家、卖家和商品

### messages表
- 消息通知，包含消息类型、标题、内容等 