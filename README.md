# 酒店管理系统

基于 Spring Boot + Vue 3 + MySQL 的酒店管理系统，支持客房管理、预订、入住登记、结账、换房等核心功能。

## 技术栈

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 渐进式 JavaScript 框架 |
| Vite | 5.4+ | 前端构建工具 |
| Element Plus | 2.7+ | UI 组件库 |
| Vue Router | 4.3+ | 路由管理 |
| Pinia | 2.1+ | 状态管理 |

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 8 | 开发语言 |
| Spring Boot | 2.7.18 | 后端框架 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| MySQL Connector | 8.0.33 | 数据库驱动 |
| Lombok | 1.18.36 | 简化代码工具 |
| FastJSON | 2.0.32 | JSON 处理 |
| Spring Security Crypto | - | BCrypt 密码加密 |

### 数据库

| 技术 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0+ | 关系型数据库 |

## 环境要求

- **JDK** 8 或以上
- **Node.js** 16 或以上
- **MySQL** 8.0 或以上
- **Maven**（可选，项目已内置 Maven Wrapper）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/你的用户名/你的仓库名.git
cd 你的仓库名
```

### 2. 初始化数据库

启动 MySQL 服务后，执行初始化脚本创建数据库和表：

```bash
mysql -u root -p < hotel-management-system/sql/init.sql
```

脚本会自动创建 `hotel_management` 数据库及所有表，并插入默认管理员账号。

> 默认数据库连接配置在 `hotel-management-system/src/main/resources/application.yml`，用户名 `root`，密码 `123456`，端口 `3306`。如果你的 MySQL 配置不同，请修改该文件。

### 3. 启动后端

```bash
cd hotel-management-system
.\mvnw.cmd spring-boot:run       # Windows
# ./mvnw spring-boot:run         # macOS / Linux
```

后端启动在 http://localhost:8080

### 4. 启动前端

新打开一个终端：

```bash
cd hotel-frontend
npm install    # 首次运行需要安装依赖
npm run dev
```

前端启动在 http://localhost:5173，浏览器会自动打开。

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

首次登录后请立即修改密码。

## 项目结构

```
project/
├── hotel-frontend/              # 前端项目
│   ├── src/                     # 源代码
│   ├── index.html               # 入口 HTML
│   ├── package.json             # 依赖配置
│   └── vite.config.js           # Vite 配置
├── hotel-management-system/     # 后端项目
│   ├── src/                     # 源代码
│   ├── sql/                     # 数据库初始化脚本
│   │   └── init.sql
│   ├── pom.xml                  # Maven 配置
│   └── mvnw.cmd                 # Maven Wrapper (Windows)
└── README.md
```
