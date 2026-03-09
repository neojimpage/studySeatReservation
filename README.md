\# 校园自习座位预约系统



\## 项目简介

校园自习座位预约系统是面向高校学生的轻量级预约管理平台，旨在解决图书馆、自习室座位占座混乱、找座耗时、管理效率低等校园痛点。本项目为 6 周实训 MVP 版本，聚焦核心预约流程，实现座位分区选择、时段预约、暂离报备、超时释放等关键功能，适配校园真实使用场景，支持学生端自助预约，整体架构轻量化、易部署。



\## 核心功能

\### 👨‍🎓 学生端

\- 座位可视化选择：按楼层/区域展示座位状态（空闲/已预约/占用）

\- 时段预约：支持选择未来 7 天内的自习时段（默认每段 2 小时，可配置）

\- 暂离报备：临时离开可报备，系统保留座位 30 分钟

\- 预约记录：查看历史预约、当前有效预约，支持取消未开始的预约

\- 违规提醒：超时未离场累计 3 次，限制次日预约权限



\### 📌 非功能特性

\- 轻量化：无复杂依赖，本地/校园服务器均可部署

\- 响应式：适配电脑、手机等多终端访问

\- 低耦合：前后端分离，便于分工开发和功能扩展



\## 技术栈

\### 后端

\- 开发语言：Java 17+

\- 框架：Spring Boot 3.2（核心）、Spring MVC、MyBatis-Plus（数据访问）

\- 数据库：MySQL 8.0（核心数据）

\- 构建工具：Maven 3.8+

\- API规范：RESTful API，支持JSON格式交互

\- 其他：Lombok（简化代码）、Spring Validation（参数校验）



\### 前端

\- 核心框架：Vue 3 + Element Plus（或HTML+CSS+JS原生，降低难度）

\- 构建工具：npm 8+ / yarn

\- 适配：Flex布局实现响应式界面



\### 部署

\- 开发环境：Windows

\- 运行环境：本地JDK 17+ / Tomcat 10+



\## 快速开始

\### 前置条件

\- 安装 JDK 17+、Maven 3.8+、MySQL 8.0

\- 前端需安装 Node.js 16+

\- 配置 MySQL：创建数据库 `campus\_seat\_db`，字符集设为 `utf8mb4`



\### 本地运行（Java+Spring Boot 版本）

1\. 克隆/下载代码（实训阶段直接本地创建项目）

&nbsp;  ```bash

&nbsp;  # 实训中可省略克隆，直接在IDEA中创建Spring Boot项目

&nbsp;  git clone <项目仓库地址>

&nbsp;  cd campus-seat-reservation

2\.配置数据库连接（修改 backend/src/main/resources/application.yml）

yaml

spring:

&nbsp; datasource:

&nbsp;   url: jdbc:mysql://localhost:3306/campus\_seat\_db?useUnicode=true\&characterEncoding=utf8mb4\&useSSL=false\&serverTimezone=Asia/Shanghai

&nbsp;   username: root  # 替换为你的MySQL用户名

&nbsp;   password: 123456  # 替换为你的MySQL密码

&nbsp;   driver-class-name: com.mysql.cj.jdbc.Driver

&nbsp; mybatis-plus:

&nbsp;   mapper-locations: classpath:mapper/\*.xml

&nbsp;   type-aliases-package: com.campus.seat.entity

3\.启动后端服务

方式 1（IDEA）：直接运行 BackendApplication.java 主类

方式 2（Maven 命令）：

bash

运行

cd backend

mvn clean compile

mvn spring-boot:run

4\.初始化基础数据

项目启动后，访问 http://localhost:8080/api/init/data（需提前编写初始化接口）

自动创建管理员账号：admin / 123456

5\.启动前端服务（若用 Vue）

bash

运行

cd frontend

npm install  # 安装依赖

npm run dev  # 启动开发服务器

6\.访问系统

学生端：http://localhost:5173（前端） / http://localhost:8080/api（后端接口）



项目目录结构（参考）

plaintext

campus-seat-reservation/

├── backend/                # 后端Spring Boot项目

│   ├── src/main/java/com/campus/seat/

│   │   ├── controller/     # 控制器（接口层）

│   │   ├── service/        # 服务层（业务逻辑）

│   │   ├── mapper/         # Mapper层（数据访问）

│   │   ├── entity/         # 实体类（数据库映射）

│   │   ├── dto/            # 数据传输对象

│   │   ├── config/         # 配置类（MyBatis、跨域等）

│   │   ├── utils/          # 工具类（时间、加密、响应结果）

│   │   └── BackendApplication.java  # 启动类

│   ├── src/main/resources/

│   │   ├── application.yml # 核心配置

│   │   ├── mapper/         # MyBatis XML映射文件

│   │   └── static/         # 静态资源（可选）

│   └── pom.xml             # Maven依赖

├── frontend/               # 前端Vue项目

│   ├── src/                # 源码（页面、组件、接口请求）

│   │   ├── views/          # 页面视图

│   │   ├── components/     # 通用组件

│   │   ├── api/            # 接口请求封装

│   │   └── main.js         # 前端入口

│   └── package.json        # 前端依赖

├── docs/                   # 文档（需求说明、测试用例、实训报告）

└── README.md               # 项目说明（本文档）



团队分工（适配 4-5 人实训团队）

表格

角色	核心职责	实训阶段产出物

项目经理（1 人）	需求梳理、进度管控、文档编写、对接评审	项目启动书、周进度报告、最终实训报告

后端开发（1-2 人）	数据库设计、接口开发、业务逻辑实现	后端代码、接口文档、数据库表结构

前端开发（1 人）	页面开发、交互实现、多终端适配	前端代码、原型图、页面交互说明

测试 / 运维（1 人）	功能测试、Bug 修复验证、部署文档编写	测试用例、Bug 清单、部署手册



注意事项

聚焦 MVP：优先实现核心预约功能，暂不开发扫码签到、人脸识别等非核心功能；

代码规范：遵循阿里巴巴 Java 开发手册，统一变量命名、注释格式，每周提交代码并标注版本；

依赖管理：在 pom.xml 中仅引入必要依赖（Spring Boot Web、MyBatis-Plus、MySQL 驱动、Lombok），避免冗余；

文档同步：功能开发与文档编写同步进行，避免最后集中补文档；

问题记录：建立 Bug 清单，记录解决过程，便于实训报告总结。

许可证

本项目为高校实训项目，采用 MIT 许可证，仅供学习使用。

