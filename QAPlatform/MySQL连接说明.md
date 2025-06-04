# MySQL数据库连接说明

## 请尝试以下方法连接MySQL并创建数据库：

### 方法1: 使用你的密码
```bash
mysql -u root -ppassword < mysql_setup.sql
```

### 方法2: 交互式输入密码
```bash
mysql -u root -p < mysql_setup.sql
# 然后输入密码：password
```

### 方法3: 如果密码不对，尝试重置MySQL密码
```bash
# 停止MySQL服务
brew services stop mysql@8.0

# 安全模式启动MySQL
mysqld_safe --skip-grant-tables &

# 连接并重置密码
mysql -u root -e "FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY 'password';"

# 重启MySQL服务
brew services restart mysql@8.0
```

### 方法4: 检查MySQL配置
```bash
# 检查MySQL进程
ps aux | grep mysql

# 检查配置文件
cat /opt/homebrew/etc/my.cnf
```

### 方法5: 如果以上都不行，使用MySQL Workbench或其他图形工具
- 连接到 localhost:3306
- 用户名: root  
- 密码: password
- 执行SQL: `CREATE DATABASE qa_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

## 数据库创建成功后：
1. 重新编译并启动应用: `mvn package -DskipTests && java -jar target/qa-platform-1.0.0.jar`
2. 访问前端: http://localhost:5174/
3. 现在应该可以正常注册和登录了！

## 如果仍有问题，我们可以暂时使用H2数据库
修改 `src/main/resources/application.properties`，注释掉MySQL配置，启用H2配置即可。 