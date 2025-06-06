# 项目目录结构

```
HomeManagement/
├── bin/                   # 可执行文件目录
├── build/                 # 构建输出目录
├── demo/                  # 示例代码
├── gradle/                # Gradle包装器目录
├── log/                   # 日志文件目录
├── src/                   # 后端源代码
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── chii/
│   │   │           └── homemanagement/
│   │   │               ├── common/        # 通用类和工具
│   │   │               ├── config/        # 配置类
│   │   │               ├── controller/    # 控制器
│   │   │               ├── entity/        # 实体类
│   │   │               ├── exception/     # 异常处理
│   │   │               ├── mapper/        # MyBatis映射器
│   │   │               ├── service/       # 服务层
│   │   │               ├── util/          # 工具类
│   │   │               └── HomeManagementApplication.java # 主应用类
│   │   └── resources/
│   │       ├── log4j2/      # 日志配置
│   │       ├── templates/   # 模板文件
│   │       ├── application.yml    # 基础应用配置
│   │       ├── application-dev.yml # 开发环境配置
│   │       ├── application-prod.yml # 生产环境配置
│   │       └── schema.sql   # 数据库脚本
│   └── test/                # 测试代码
├── uploads/                # 上传文件目录
├── vue3/                   # 前端项目
│   ├── build/              # 构建配置
│   ├── dist/               # 构建输出
│   ├── public/             # 公共资源
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── assets/         # 静态资源
│   │   ├── components/     # 通用组件
│   │   ├── config/         # 配置文件
│   │   ├── directives/     # 自定义指令
│   │   ├── i18n/           # 国际化
│   │   ├── layout/         # 布局组件
│   │   ├── plugins/        # 插件
│   │   ├── router/         # 路由配置
│   │   ├── store/          # 状态管理
│   │   ├── style/          # 样式文件
│   │   ├── types/          # TypeScript类型定义
│   │   ├── utils/          # 工具函数
│   │   ├── views/          # 页面组件
│   │   ├── App.vue         # 主应用组件
│   │   ├── http.ts         # HTTP请求封装
│   │   └── main.ts         # 应用入口
│   ├── types/              # 全局类型定义
│   ├── .env                # 环境变量
│   ├── .env.development    # 开发环境变量
│   ├── .env.production     # 生产环境变量
│   ├── .env.staging        # 测试环境变量
│   ├── index.html          # HTML模板
│   ├── package.json        # 依赖配置
│   ├── tailwind.config.ts  # Tailwind配置
│   ├── tsconfig.json       # TypeScript配置
│   └── vite.config.ts      # Vite配置
├── .gitignore              # Git忽略配置
├── build.gradle            # Gradle构建配置
├── gradlew                 # Gradle包装器脚本
├── gradlew.bat             # Gradle包装器脚本(Windows)
├── README.md               # 项目说明
└── settings.gradle         # Gradle设置
``` 


# 家庭管理系统设计文档

## 1. 系统概述

家庭管理系统（Home Management System）是一个综合性家庭事务管理平台，旨在帮助用户高效管理家庭物品和空间、记录财务状况、规划家庭活动，以及维护家庭成员信息。本系统采用前后端分离架构，后端使用Java Spring Boot技术栈，前端采用Vue3+TypeScript开发。系统特别聚焦于家庭物品管理，通过标签、分类和层级结构，帮助用户更好地组织和查找家庭物品。

## 2. 技术架构

### 2.1 后端架构

- **开发语言**：Java 21
- **框架**：Spring Boot 3.2.x
- **数据库**：MySQL 8.0
- **ORM框架**：MyBatis-Plus
- **API风格**：RESTful
- **日志系统**：Log4j2、Slf4j
- **安全框架**：Spring Security
- **API文档**：Swagger (OpenAPI 3)
- **参数验证**：Jakarta Validation

### 2.2 前端架构

- **框架**：Vue3
- **开发语言**：TypeScript
- **构建工具**：Vite
- **UI组件库**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **CSS框架**：Tailwind CSS
- **国际化**：Vue I18n
- **图表库**：ECharts

## 3. 系统模块

### 3.1 用户管理模块

- **功能**：用户注册、登录、权限管理、个人信息维护
- **关键实体**：User, Role, Permission
- **API路径**：`/api/user/**`, `/api/auth/**`
- **控制器**：UserController, AuthController

### 3.2 实体管理模块

- **功能**：实体（物品/空间）管理、标签管理、图片管理、维护记录管理
- **关键实体**：Entity, Tag, EntityTag, EntityImage, EntityMaintenance
- **API路径**：`/api/entities/**`, `/api/tags/**`, `/api/entity-images/**`, `/api/entity-maintenance/**`
- **控制器**：EntityController, TagController, EntityImageController, EntityMaintenanceController

### 3.3 提醒管理模块

- **功能**：物品保修到期提醒、维护提醒、其他重要日期提醒
- **关键实体**：Reminder
- **API路径**：`/api/reminders/**`
- **控制器**：ReminderController

### 3.4 仪表盘模块

- **功能**：系统概览、物品统计、状态分布、标签分布、最近添加的实体和提醒
- **API路径**：`/api/dashboard/**`
- **控制器**：DashboardController

### 3.5 系统设置模块

- **功能**：系统参数设置、用户偏好设置
- **关键实体**：SystemSetting
- **API路径**：`/api/settings/**`
- **控制器**：SettingController

## 4. 数据模型

### 4.1 用户与权限相关

- **User**: 
  - 用户基本信息，包括id、username、password、email、phone、nickname、avatar、role、status等
  - role字段直接保存角色类型，如ADMIN、USER，无需关联角色表
  - `src/main/java/com/chii/homemanagement/entity/User.java`

### 4.2 实体（物品/空间）相关

- **Entity**: 
  - 实体（物品/空间）基本信息，包括id、name、type、code、specification、quantity、price、productionDate、purchaseDate、warrantyPeriod、warrantyEndDate、usageFrequency、usageYears、userId、parentId、level、path、sort、status、description等
  - 支持树形结构，通过parentId、level、path字段构建层级关系
  - status字段表示状态，包括normal、damaged、discarded、lent、expired
  - `src/main/java/com/chii/homemanagement/entity/Entity.java`

- **Tag**: 
  - 标签信息，包括id、name、color、userId等
  - 标签可用于标记实体，便于分类查询
  - `src/main/java/com/chii/homemanagement/entity/Tag.java`

- **EntityTag**: 
  - 实体与标签的多对多关联表
  - 包括id、entityId、tagId等
  - `src/main/java/com/chii/homemanagement/entity/EntityTag.java`

- **EntityImage**: 
  - 实体图片信息，包括id、entityId、imageUrl、imageData、imageType等
  - 图片类型包括normal、receipt（购买凭证）、warranty（保修凭证）
  - `src/main/java/com/chii/homemanagement/entity/EntityImage.java`

- **EntityMaintenance**: 
  - 实体维护记录，包括id、entityId、maintenanceDate、maintenanceType、cost、description、result等
  - 维护类型包括repair、clean、check
  - `src/main/java/com/chii/homemanagement/entity/EntityMaintenance.java`

### 4.3 提醒相关

- **Reminder**: 
  - 提醒信息，包括id、entityId、userId、content、remindDate、type、status、notificationMethods、daysInAdvance、isRecurring、recurringCycle等
  - 提醒类型包括warranty（保修到期）、maintenance（维护提醒）、other（其他）
  - `src/main/java/com/chii/homemanagement/entity/Reminder.java`

### 4.4 系统设置相关

- **SystemSetting**: 
  - 系统设置信息，包括id、type、userId、settingKey、settingValue、name、defaultValue等
  - 设置类型包括SYSTEM（系统设置）、USER（用户个人设置）
  - `src/main/java/com/chii/homemanagement/entity/SystemSetting.java`

## 5. 接口设计

系统API遵循RESTful设计规范，主要包括以下接口类型：

- **GET**：获取资源
- **POST**：创建资源
- **PUT**：更新资源
- **DELETE**：删除资源

接口响应格式统一为：

```json
{
  "code": 200,      // 状态码：200成功，其他表示不同错误
  "message": "操作成功", // 响应消息
  "data": {}        // 响应数据
}
```

### 5.1 实体管理接口

#### 实体基本操作

- `GET /api/entities/page`: 分页查询实体列表
- `GET /api/entities/{id}`: 查询实体详情
- `POST /api/entities`: 新增实体
- `PUT /api/entities/{id}`: 更新实体
- `DELETE /api/entities/{id}`: 删除实体

#### 实体查询操作

- `GET /api/entities/tree`: 获取实体树
- `GET /api/entities/list/by-parent`: 获取子实体列表
- `GET /api/entities/list/by-user`: 获取用户使用的物品列表
- `GET /api/entities/search`: 搜索实体
- `GET /api/entities/list/by-status`: 根据状态获取物品列表
- `GET /api/entities/list/expiring`: 获取即将过保的物品列表
- `GET /api/entities/list/expired`: 获取已过保的物品列表
- `GET /api/entities/list/by-tag`: 根据标签获取实体列表
- `GET /api/entities/list/by-date-range`: 根据购买日期范围获取物品列表
- `GET /api/entities/list/by-price-range`: 根据价格范围获取物品列表
- `GET /api/entities/{id}/tags`: 获取实体的标签
- `GET /api/entities/recent`: 获取最近添加的实体列表

### 5.2 仪表盘接口

- `GET /api/dashboard/statistics`: 获取仪表盘统计数据
- `GET /api/dashboard/recent-entities`: 获取最近添加的实体
- `GET /api/dashboard/recent-reminders`: 获取最近的提醒
- `GET /api/dashboard/stat/by-parent`: 根据父实体统计子实体
- `GET /api/dashboard/stat/by-tag`: 根据标签统计物品
- `GET /api/dashboard/stat/by-usage-frequency`: 根据使用频率统计物品
- `GET /api/dashboard/sum-value`: 统计物品总价值

### 5.3 用户认证接口

- `POST /api/auth/login`: 用户登录
- `POST /api/auth/register`: 用户注册
- `POST /api/auth/logout`: 用户登出
- `GET /api/auth/info`: 获取当前用户信息

### 5.4 其他接口

- `GET /api/tags`: 获取标签列表
- `POST /api/reminders`: 创建提醒
- `GET /api/settings`: 获取系统设置

## 6. 安全设计

- **认证**：JWT (JSON Web Token) 基于token的认证机制
- **授权**：基于角色的访问控制 (RBAC)
- **加密**：密码使用BCrypt加密存储
- **安全防护**：防止XSS、CSRF、SQL注入等常见安全威胁
- **接口安全**：使用Spring Security保护API接口
- **文件上传安全**：限制文件类型和大小，使用随机文件名存储，防止路径遍历攻击

## 7. 性能优化

- **数据库优化**：
  - 合理建立索引，如在Entity表的type、status、userId字段上建立索引
  - 使用MyBatis-Plus提供的分页查询功能
  - 优化查询SQL，避免多表关联查询

- **缓存策略**：
  - 缓存实体树结构和统计数据

- **前端优化**：
  - 代码分割，懒加载组件
  - 资源压缩和合并
  - 图片懒加载和优化
  - 使用虚拟滚动处理大量数据的列表

## 8. 部署架构

- **开发环境**：本地开发环境
- **测试环境**：内部测试服务器
- **生产环境**：服务器部署

## 9. 开发规范

### 9.1 后端开发规范

- **命名规范**：
  - 类名：大驼峰命名法（如UserService）
  - 方法名：小驼峰命名法（如getUserById）
  - 常量：全大写下划线分隔（如MAX_SIZE）
- **代码组织**：按功能模块和层次结构组织
- **异常处理**：统一异常处理，不允许吞异常
- **日志规范**：合理使用日志级别，关键操作必须记录日志
- **接口文档**：使用Swagger注解维护API文档
- **参数验证**：使用Jakarta Validation进行参数校验

### 9.2 前端开发规范

- **命名规范**：
  - 组件名：大驼峰命名法（如UserProfile）
  - 变量/函数：小驼峰命名法（如userData）
  - CSS类名：中划线命名法（如user-container）
- **代码组织**：按功能模块组织
- **状态管理**：合理使用Pinia管理状态
- **UI规范**：遵循Element Plus设计规范，保持界面一致性
- **响应式设计**：适配不同屏幕尺寸

## 10. 文件存储规范

- **上传文件存储路径**：`/uploads/{module}/{userId}/{filename}`
- **文件类型限制**：限制上传文件类型，只允许图片和文档
- **文件大小限制**：图片不超过5MB，文档不超过20MB
- **图片处理**：上传图片自动压缩和生成缩略图
- **文件命名**：使用UUID等随机字符串重命名文件，避免文件名冲突

## 11. 集成与通信

- **内部服务通信**：RESTful API
- **外部系统接口**：预留标准化接口服务
- **消息通知**：支持系统内通知、邮件通知
