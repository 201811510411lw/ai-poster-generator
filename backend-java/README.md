# AI Poster Generator Java Backend

Java Spring Boot 主后端，负责登录认证、用户、素材、任务、文件存储以及后续调用 Python AI 服务。

## 技术栈

- JDK 21
- Spring Boot 3.3.13
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- BCrypt

## 本地启动

### 1. 准备 MySQL

执行初始化脚本：

```bash
mysql -uroot -p < src/main/resources/db/init.sql
```

默认测试账号：

```text
用户名：admin
密码：admin123
```

### 2. 配置环境变量

可选配置：

```bash
export MYSQL_URL='jdbc:mysql://localhost:3306/ai_poster_generator?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai'
export MYSQL_USERNAME='root'
export MYSQL_PASSWORD='root'
export JWT_SECRET='change-me-change-me-change-me-change-me-change-me-change-me'
```

### 3. 启动服务

```bash
mvn spring-boot:run
```

默认端口：

```text
http://localhost:8080
```

## 已实现接口

### 登录

```http
POST /api/auth/login
```

请求：

```json
{
  "username": "admin",
  "password": "admin123",
  "remember": true
}
```

返回：

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "登录成功",
  "data": {
    "token": "...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": null,
      "role": "ADMIN"
    }
  }
}
```

### 当前用户

```http
GET /api/auth/me
Authorization: Bearer <token>
```

### 退出登录

```http
POST /api/auth/logout
Authorization: Bearer <token>
```

当前退出登录接口是无状态 JWT 模式，后端直接返回成功。后续如需强制 token 失效，可以接入 Redis token 黑名单。

## 前端联调

前端 `.env`：

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK_AUTH=false
```
