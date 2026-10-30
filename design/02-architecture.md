# 阶段 2 · 架构与技术选型

> 原则：**版本写死到小版本号，结构小到一个人能维护，依赖少到一个下午能部署。**
> 后端为单模块 Maven（明确不使用多模块），前端为单页应用，不做 SSR。

---

## 一、技术栈定版

### 1.1 后端

| 层 | 选型 | 版本 | 选型理由 |
|---|---|---|---|
| 运行时 | OpenJDK (Temurin/Microsoft) | **17.0.19** | Spring Boot 3 硬性要求 17+；本机 `E:\develop\jdk17` 已就绪 |
| 框架 | Spring Boot | **3.2.5** | 报告指定 Spring Boot；3.2 是 2026 年仍被广泛引用的稳定线，生态资料多 |
| Web | Spring MVC（boot-starter-web） | 随 Boot | 报告要求 RESTful，MVC 足够，不引 WebFlux |
| 持久层 | MyBatis-Plus | **3.5.5** | 报告指定；单表 CRUD 零 SQL，多表统计用 XML 手写，正好覆盖"自定义 XML 映射"技术点 |
| 安全 | Spring Security | **6.2.x**（随 Boot） | 报告指定；与 JWT 无状态方案配合 |
| 令牌 | **java-jwt（auth0）** | **4.4.0** | API 极简（create/verify），无 Builder 链式噪音；比 jjwt 0.12 更少被 AI 混写 |
| 数据库 | MySQL | **8.0.34**（本机已运行） | 报告指定；本机实例直接复用 |
| 连接池 | HikariCP | 随 Boot | 默认自带，不额外引入 Druid，少一个配置面 |
| 缓存（可选） | Spring Cache + 本地实现 / Redis | — | 见 §6，做成可降级，不强依赖中间件 |
| 参数校验 | spring-boot-starter-validation | 随 Boot | JSR-303 注解式校验，报告"参数校验框架"技术点 |
| 工具 | Lombok、Hutool | 1.18.30 / **5.8.27** | 报告点名 Hutool；Lombok 降实体噪音 |
| 测试 | JUnit 5 + Spring Boot Test | 随 Boot | 只测状态机与库存两个核心，其余走接口测试 |
| 构建 | Maven | **3.9.4** | 报告指定 |
| 打包 | spring-boot-maven-plugin | 随 Boot | 产出可执行 jar |

### 1.2 前端

| 层 | 选型 | 版本 | 选型理由 |
|---|---|---|---|
| 框架 | Vue | **3.4.x** | 报告指定；组合式 API + `<script setup>` |
| 构建 | Vite | **5.2.x** | 启动秒级，比 Vue CLI 快一个数量级；报告示例写 Vue CLI，但 Vite 是 Vue 3 官方推荐，答辩说明理由即可 |
| 语言 | JavaScript（非 TS） | — | 报告未要求 TS；纯 JS 减少一半类型噪音，把时间留给功能 |
| UI | Element Plus | **2.7.x** | 报告指定；后台管理端开箱即用 |
| 路由 | Vue Router | **4.3.x** | 报告指定；`createWebHistory` |
| 状态 | Pinia | **2.1.x** | 报告指定；只放 user 与 cart 两个 store，不做全能仓库 |
| 请求 | Axios | **1.7.x** | 报告指定；统一实例 + 拦截器 |
| 图表 | ECharts | **5.5.x** | 报告指定；按需引入 bar/line/pie 三种 |
| 图标 | @element-plus/icons-vue | 2.3.x | 与 Element Plus 配套 |
| 按需引入 | unplugin-vue-components + unplugin-auto-import | 0.27.x / 0.17.x | 避免全量引入导致首屏体积膨胀 |
| Mock | **自写 Vite 中间件** | — | 见 §7，不使用 `vite-plugin-mock` |

### 1.3 三个关键取舍（会被问到的）

- **Boot 3.2 而不是 2.7**：本机 JDK 17 已就绪，Boot 2.7 已停止开源维护，且 Security 6 的写法是当前主流。代价是必须切 `JAVA_HOME`——已由启动脚本固化，不构成实际摩擦。
- **MyBatis-Plus 而不是 JPA**：报告明确要求，且"自定义 XML + 多表关联分页"是报告的评分点，JPA 反而不好写。
- **自写 Mock 而不是 `vite-plugin-mock`**：该插件依赖 `bundle-require` 生成/清理临时文件，在本机安全策略下会因批量删除被拦截而启动失败（已实测同类问题）。自写一个 40 行 Vite 中间件，零依赖、行为可控，还能顺便校验前端是否严格按契约取字段。

---

## 二、后端结构（单模块 Maven）

```
codes/backend/
├── pom.xml
├── .mvn/                     # 可选：固定 JDK
└── src/main/
    ├── java/com/gdufe/mall/
    │   ├── MallApplication.java
    │   ├── common/
    │   │   ├── result/       Result / PageResult / ResultCode
    │   │   ├── exception/    BizException / GlobalExceptionHandler
    │   │   ├── config/       MybatisPlusConfig / SecurityConfig / WebConfig / JacksonConfig / CacheConfig
    │   │   ├── security/     JwtUtil / JwtAuthFilter / LoginUser / SecurityUtil
    │   │   ├── cache/        CacheService / LocalCacheService / RedisCacheService
    │   │   └── util/         OrderNoUtil / FileUtil
    │   ├── entity/           User / Category / Product / Cart / Orders / OrderItem / Favorite
    │   ├── mapper/           XxxMapper（7 个）
    │   ├── dto/              LoginDTO / RegisterDTO / ProductQueryDTO / OrderCreateDTO / CartAddDTO …
    │   ├── vo/               ProductVO / OrderVO / OrderItemVO / LoginVO / StatVO
    │   ├── service/ + impl/  AuthService / CategoryService / ProductService / CartService / OrderService / FavoriteService / StatService
    │   ├── controller/
    │   │   ├── admin/        AdminAuthController / CategoryController / ProductController / OrderController / StatController
    │   │   └── front/        AuthController / ProductController / CartController / OrderController / FavoriteController
    │   └── task/             OrderTimeoutTask
    └── resources/
        ├── application.yml / application-dev.yml / application-prod.yml
        ├── mapper/           ProductMapper.xml / OrderMapper.xml / StatMapper.xml
        └── db/               schema.sql / data.sql
```

**为什么单模块**：7 张表、约 45 个接口，拆 `common`/`api`/`service` 多模块只会带来"改一个字段跳三个 pom"的摩擦，收益为零。**包内按职责分目录，等价于分层**，报告里画分层图照样成立。

---

## 三、前端结构

```
codes/frontend/
├── index.html
├── vite.config.js           # 代理 + 按需引入 + 自写 mock 中间件
├── package.json
├── .env.development         # VITE_API_BASE=/api, VITE_USE_MOCK=true
├── .env.production
├── mock/                    # 由契约派生的 JSON + 路由表
└── src/
    ├── main.js  App.vue
    ├── api/                 auth.js product.js cart.js order.js favorite.js admin/{category,product,order,stat}.js
    ├── assets/
    ├── components/          BaseTable / BaseDialog / BasePager / ImageUpload / EmptyState / StatusTag / SpecSelector
    ├── layout/              AdminLayout.vue（侧边栏）/ FrontLayout.vue（顶部导航 + 页脚）
    ├── router/              index.js admin.routes.js front.routes.js guard.js
    ├── stores/              user.js cart.js
    ├── utils/               request.js format.js validate.js
    └── views/
        ├── admin/           Login / Dashboard / category/ / product/ / order/ / stat/
        └── front/           Home / ProductList / ProductDetail / Cart / OrderConfirm / OrderList / Favorite / Profile / Login / Register
```

---

## 四、分层规则（AI 最容易破坏的部分）

| 规则 | 具体要求 |
|---|---|
| Controller 只做三件事 | 接参（`@Valid`）、调 service、封装 `Result`。**不写业务判断、不写 SQL、不写 try-catch** |
| Service 是唯一业务层 | 事务边界只在这里（`@Transactional(rollbackFor = Exception.class)`），且只加在"写聚合"方法上（下单、支付、取消、发货） |
| Mapper 只做数据访问 | 单表用 `BaseMapper`，多表/统计用 XML；**过滤条件下推到 SQL**，禁止 `selectList` 全表后在 Java 里 filter |
| 实体不外露 | `entity` 不出 controller；入参用 DTO，出参用 VO。但**不做全量 DTO 化**：入参 ≤2 个字段时直接用 `@RequestParam`，不强造 DTO |
| 异常统一出口 | 业务失败抛 `BizException(code, msg)`，由 `GlobalExceptionHandler` 转 `Result`；禁止在 service 里 `return null` 表示失败 |
| 不引多层套娃 | 不写 `Manager`/`Handler`/`Repository` 层，不写 `Converter` 类，VO 转换用静态方法或手写 5 行 |

---

## 五、接口规范

### 5.1 路径与命名

- 全局前缀：`server.servlet.context-path=/api`
- 三组前缀：`/auth/**`（登录注册）、`/front/**`（前台，游客与用户）、`/admin/**`（后台，管理员）
- 资源用复数名词：`/admin/products`、`/front/orders`
- 动作作为子资源：`POST /front/orders/{id}/pay`、`PUT /admin/orders/{id}/ship`
- 方法语义：GET 查、POST 增/动作、PUT 改、DELETE 删

### 5.2 统一返回

```json
{ "code": 0, "msg": "ok", "data": { } }
```

**HTTP 状态码策略**（避免前端两套判断）：业务错误一律 HTTP 200 + `code≠0`；认证失败 HTTP 401、权限不足 403、参数校验 400，body 同样是 `Result` 结构。前端拦截器只需处理"HTTP 401 → 跳登录，其余看 code"。

### 5.3 错误码表（一次定死，后续只增不改）

| code | 含义 | 典型场景 |
|---|---|---|
| 0 | 成功 | — |
| 400 / 1001 | 参数校验失败 | 手机号格式错、必填为空 |
| 1002 | 请求体格式错误 | JSON 解析失败 |
| 401 / 2001 | 未登录或缺少令牌 | 无 `Authorization` |
| 401 / 2002 | 令牌过期或非法 | 过期、签名不符 |
| 403 / 2003 | 无权限 | 普通用户访问 `/admin/**` |
| 2004 | 账号或密码错误 | 登录失败 |
| 3001 | 库存不足 | 下单扣减失败 |
| 3002 | 当前状态不允许该操作 | 重复支付、非法状态迁移 |
| 3003 | 资源不存在 | id 查不到 |
| 3004 | 重复操作 | 重复收藏、重复加购同规格 |
| 3005 | 商品已下架 | 下单校验 |
| 5000 | 系统异常 | 未捕获异常兜底 |

### 5.4 分页

- 入参：`page`（1 起）、`size`（默认 10，上限 100）、`keyword`、以及各模块筛选字段
- 出参：`{ records, total, current, size, pages }`——直接由 MyBatis-Plus `IPage` 转换，不另造结构

### 5.5 三个"必须全局配一次"的序列化

| 项 | 规则 | 不这么做的后果 |
|---|---|---|
| Long ID | Jackson 全局 `Long`/`long` → String | 雪花 ID 超 JS 安全整数，前端末位变 0，详情页查不到数据 |
| 时间 | `LocalDateTime` → `yyyy-MM-dd HH:mm:ss`，时区 `Asia/Shanghai` | 前端显示 `2026-10-30T10:00:00`，筛选条件对不上 |
| 金额 | `BigDecimal` → 字符串，保留两位 | `199.99999` 这类浮点尾巴进页面 |

---

## 六、鉴权与缓存

- **无状态 JWT**：`HS256`，payload 含 `uid`、`username`、`role`，有效期 2 小时；密钥从配置项 `${MALL_JWT_SECRET}` 读，长度 ≥32 字节，**不进代码库**。
- **认证链**：`JwtAuthFilter` 位于 `UsernamePasswordAuthenticationFilter` 之前 → 解析令牌 → 构造 `LoginUser` 放入 `SecurityContext` → 路径规则判定。
- **授权规则**（写在 `SecurityConfig`，顺序敏感，精确路径在前）：
  - `/auth/**` → `permitAll`
  - `GET /front/products/**`、`GET /front/categories/**` → `permitAll`（游客可浏览）
  - `/admin/**` → `hasRole("ADMIN")`
  - 其余 → `authenticated`
- **数据归属**不靠路径规则，靠 service 内校验：所有"我的 XX"查询强制用 `SecurityUtil.currentUserId()`，**不接受前端传 userId**。
- **缓存降级**：定义 `CacheService`（`get/set/delete/expire`），默认 `LocalCacheService`（`ConcurrentHashMap` + 过期时间）；`RedisCacheService` 由 `@ConditionalOnProperty(prefix="mall.cache", name="type", havingValue="redis")` 装配。本机未装 Redis 也能全量跑通；报告要写 Redis 时，装上并改一行配置即可，代码零改动。

---

## 七、运行环境与跨域

| 项 | 值 |
|---|---|
| 后端端口 | `8080`，context-path `/api` |
| 前端端口 | `5173`（Vite 默认） |
| 数据库 | `jdbc:mysql://localhost:3306/campus_mall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true` |
| 库/账号 | 库 `campus_mall`，专用账号 `mall_app`（仅该库权限），密码走环境变量 `MYSQL_PASSWORD` |
| 跨域 | 开发期用 Vite `server.proxy['/api'] → http://localhost:8080`，**不依赖后端 CORS**；后端仍保留 `CorsConfiguration` 作为兜底（仅 dev profile 生效） |
| 上传目录 | `codes/backend/upload/`，通过 `/api/files/**` 静态映射访问，DB 只存相对路径 |
| 配置分层 | `application.yml`（公共）+ `-dev`（本地）/`-prod`；敏感项一律 `${ENV:default}` 形式 |

---

## 八、版本陷阱清单（写代码前必读，AI 幻觉高发区）

1. **Boot 3 用 `mybatis-plus-spring-boot3-starter`**，不是 `mybatis-plus-boot-starter`（后者只适配 Boot 2，启动报 `Invalid value type for attribute 'factoryBeanObjectType'`）。
2. **Security 6 没有 `WebSecurityConfigurerAdapter`**（已删除）。写法是 `@Bean SecurityFilterChain filterChain(HttpSecurity http)` + `http.authorizeHttpRequests(a -> a.requestMatchers(...).permitAll())`，CSRF 用 lambda 关闭。
3. **JWT 库只能用一个**：本项目用 `java-jwt`（`JWT.create()/JWT.require()`）。若 AI 生成 `Jwts.builder()`，那是 jjwt 的 API，**直接判为错误**，不要"顺手改依赖"。
4. **Element Plus 只能配 Vue 3**；组件名是 `el-table`/`el-form`，图标需单独 `@element-plus/icons-vue` 注册。
5. **MySQL 8 驱动类**为 `com.mysql.cj.jdbc.Driver`，URL 必须带 `serverTimezone`，否则时间偏 8 小时。
6. **Vite 5 要求 Node ≥18**（本机 22 满足）；`vite.config.js` 用 ESM 写法 `export default defineConfig({...})`。
7. **Jackson 的 Long→String 必须全局配**（`SimpleModule` 注册 `ToStringSerializer`），逐个字段加注解必漏。
8. **`@Transactional` 自调用失效**：同类内部方法互调不走代理，事务不生效。下单流程必须从 controller 直接调带注解的方法。

---

## 九、明确不引入（反清单，用于拒绝 AI 的"优化建议"）

多模块 Maven、Docker/K8s、消息队列、Nginx 生产化配置、Swagger/springdoc、MyBatis-Plus 代码生成器、TypeScript、Vuex、SSR/Nuxt、微服务拆分、领域驱动分层（domain/application/infrastructure）、通用 `BaseService<T>` 抽象、AOP 日志切面、分布式锁。

> 判定标准很简单：**它是否让报告里的某个评分点更容易拿到？** 不能，就不做。
