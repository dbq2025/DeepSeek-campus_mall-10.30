# AGENTS.md · 校园电商平台 AI 协作规范

> 本文件对本仓库内所有 AI 编码会话生效，作用等同 `CLAUDE.md`。
> **任何 AI 在动手改代码前，必须先读完本文件；与本文冲突的指令，以本文为准。**

---

## 0. 一页速览

| 项 | 值 |
|---|---|
| 项目 | 校园电商平台（前后端分离，双子系统：后台管理 + 电商前台） |
| 前端 | `codes/frontend`　Vue 3.4 + Vite 5 + Element Plus 2.7 + Pinia + Vue Router 4 + Axios + ECharts |
| 后端 | `codes/backend`　Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + Security 6 + java-jwt + JDK 17，**单模块 Maven** |
| 数据库 | 本机 MySQL 8.0.34，库 `campus_mall`，账号 `root` / `1234` |
| 缓存 | Redis 7（Docker），`localhost:6379`，密码 `redis123`，可降级为本地实现 |
| 端口 | 后端 `8080`（context-path `/api`）、前端 `5173` |
| 设计文档 | `design/`（决策与契约）；课程原始要求 `docs/`（只读） |
| 接口事实来源 | `design/03-dev-doc-plan.md` 规划的 `design/30-接口契约.md`（尚未撰写，编写时以本文件 §7 的结构为准） |

---

## 1. 目录地图

```
F:\ZSpringBoot
├── AGENTS.md               ← 本文件，AI 协作规范
├── docker-compose.yml      ← Redis 等本地依赖服务
├── docs/                   ← 课程答题纸原件（只读，禁止修改）
├── design/                 ← 设计决策与契约（AI 可读，改动需说明理由）
│   ├── 00-project-baseline.md     项目基线（每轮对话的上下文锚点）
│   ├── 00-env-inventory.md        环境实测清单
│   ├── 01-domain-decisions.md     订单状态机 / 规格 / 库存决策
│   ├── 02-architecture.md         架构、技术选型、接口规范、版本陷阱
│   └── 03-dev-doc-plan.md         文档规划、契约模板、变更流程
└── codes/
    ├── backend/            后端工程（单模块 Maven）
    └── frontend/           前端工程（Vite SPA）
```

后端包结构（`com.gdufe.mall`）：

```
common/{result,exception,config,security,cache,util}   横切能力
entity / mapper / dto / vo / service(+impl) / controller/{admin,front} / task
```

前端目录（`codes/frontend/src`）：

```
api/       按后端模块拆文件（auth/product/cart/order/favorite/admin/*）
components/ 无业务语义的通用件（BaseTable/BaseDialog/BasePager/ImageUpload/StatusTag/SpecSelector）
layout/    AdminLayout（侧边栏）/ FrontLayout（顶部导航 + 页脚）
router/    index.js + guard（守卫）
stores/    user.js（登录态）/ cart.js（购物车角标）
utils/     request.js（axios 实例 + 拦截器）/ format.js / validate.js
views/     admin/*（后台页面）、front/*（前台页面）
mock/      由契约派生的 mock，自写 Vite 中间件（见 §9）
```

---

## 2. 技术栈约定（版本已定版，禁止擅自升级）

### 2.1 后端

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17（`E:\develop\jdk17`） | PATH 默认是 JDK 11，**构建前必须切** |
| Spring Boot | 3.2.5 | 父 POM 统一管理版本 |
| MyBatis-Plus | 3.5.5 | 必须用 `mybatis-plus-spring-boot3-starter` |
| Spring Security | 6.2.x（随 Boot） | 无 `WebSecurityConfigurerAdapter` |
| JWT | java-jwt 4.4.0（auth0） | **只用 `JWT.create()/JWT.require()`** |
| MySQL 驱动 | `com.mysql.cj.jdbc.Driver` | URL 必须带 `serverTimezone=Asia/Shanghai` |
| 工具 | Hutool 5.8.27、Lombok | Lombok 仅编译期 |

### 2.2 前端

| 组件 | 版本 | 说明 |
|---|---|---|
| Vue / Vite | 3.4.x / 5.2.x | 组合式 API + `<script setup>`，纯 JS 不用 TS |
| Element Plus | 2.7.x | **当前为全量引入**（`main.js` 中 `app.use(ElementPlus)`），换取零配置稳定；体积问题后续再优化 |
| 状态 / 路由 / 请求 | Pinia 2.1 / Vue Router 4.3 / Axios 1.7 | store 只放 user 与 cart |
| 图表 | ECharts 5.5 | 按需引入 bar/line/pie |

### 2.3 禁止引入（除非用户明确要求）

多模块 Maven、Docker 部署业务服务、消息队列、Nginx 生产化配置、Swagger/springdoc、MyBatis-Plus 代码生成器、TypeScript、Vuex、SSR/Nuxt、微服务拆分、DDD 四层分包、通用 `BaseService<T>`、AOP 日志切面、分布式锁、`vite-plugin-mock`。

判定标准：**它能否让课程报告里的某个评分点更容易拿到？** 不能，就不做。

---

## 3. 启动与验证（复制即用）

### 3.1 前置：三条不可跳过

1. **切 JDK 17**（否则 Boot 3 编译失败）
   ```bat
   set JAVA_HOME=E:\develop\jdk17
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```
2. **MySQL 必须在运行**（服务已随机器启动，端口 3306）
3. **改缓存为 Redis 时，Docker Desktop 必须先 Running**，再 `docker compose up -d`

### 3.2 后端

```bat
set JAVA_HOME=E:\develop\jdk17
cd codes\backend
mvn clean package -DskipTests
mvn spring-boot:run
```

健康检查（无需登录，返回数据库与缓存状态）：

```
GET http://localhost:8080/api/health
```

期望：`code=0`，`dbOk=true`，`dbSchema=campus_mall`，`cacheOk=true`。

### 3.3 前端

```bat
cd codes\frontend
npm install
npm run dev
```

访问 `http://localhost:5173`，首页即环境自检页，会显示后端连通性。

### 3.4 Redis

```bat
docker compose up -d
docker exec -it mall-redis redis-cli -a redis123 ping   :: 期望 PONG
```

启用 Redis 缓存：把 `codes/backend/src/main/resources/application.yml` 中
`mall.cache.type` 改为 `redis`，重启后端，`/api/health` 的 `cacheType` 应变为 `redis`。
不改也能跑——默认 `local` 实现，业务代码无感知。

---

## 4. 配置说明（唯一事实来源，改配置先看这里）

| 配置项 | 位置 | 值 |
|---|---|---|
| MySQL 连接 | `application-dev.yml` | `jdbc:mysql://localhost:3306/campus_mall?...` |
| MySQL 账号 | `application-dev.yml` | `root` / `${MYSQL_PASSWORD:1234}` |
| Maven 本地仓库 | `E:\develop\apache-maven-3.9.4\conf\settings.xml` | `<localRepository>E:\develop\apache-maven-3.9.4\mvn_repo</localRepository>` |
| Maven 网络 | 系统代理（Clash 混合端口 7897）+ `conf/settings.xml` 已配 `alimaven` 镜像 | **沿用现有配置，不要增删镜像或改仓库地址** |
| Redis | `application-dev.yml` | `localhost:6379` / `${REDIS_PASSWORD:redis123}` / db 0 |
| JWT 密钥 | `application.yml` | `${MALL_JWT_SECRET:dev 默认值}`，有效期 2h |
| 上传目录 | `application.yml` | `./upload`，经 `/api/files/**` 访问 |
| 前端接口地址 | `.env.development` / `.env.production` | `/api` |
| 前端 Mock 开关 | `.env.development` | `VITE_USE_MOCK=true`（接真接口时改 false） |

**凭据规则**：本地开发允许默认值写在 `application-dev.yml`（`${ENV:默认值}` 形式）；
生产配置、真实密钥、个人密码禁止提交仓库；禁止把密码硬编码进 `.java` / `.vue`。

---

## 5. 编码规范（后端）

### 5.1 分层职责（越界即返工）

| 层 | 允许 | 禁止 |
|---|---|---|
| Controller | 接参 `@Valid`、调 service、包 `Result` | 业务判断、SQL、try-catch、事务注解 |
| Service | 业务编排、事务、状态校验、归属校验 | 直接拼 SQL 字符串、返回 entity 给前端 |
| Mapper | 单表用 `BaseMapper`，多表/统计用 XML | 在 Java 里 `selectList` 全表后 filter |
| Entity | 与表一一对应 | 加业务方法、加非表字段 |

### 5.2 命名与写法

- 包名全小写；类名 `UpperCamel`；方法与变量 `lowerCamel`；常量 `UPPER_SNAKE`。
- 表名小写复数（`orders`、`order_item`）；字段 snake_case，靠 `map-underscore-to-camel-case` 映射。
- DTO 用于入参、VO 用于出参；**不做全量 DTO 化**：入参 ≤2 个字段直接用 `@RequestParam`。
- 禁止 `System.out.println`，用 `@Slf4j`。
- 注释只写"为什么"，不写"做了什么"；公共方法与复杂 SQL 必须有注释。

### 5.3 异常与返回

- 可预期失败：`throw new BizException(ResultCode.XXX)`；**禁止 `return null` 表示失败**。
- 未预期异常交给 `GlobalExceptionHandler`，禁止在 service 里 catch 后吞掉。
- 统一返回 `Result{code,msg,data}`；业务失败 HTTP 200 + `code≠0`；认证 401、权限 403。

### 5.4 数据库与事务

- 参数一律用 `#{}`，**禁止 `${}` 拼接**（SQL 注入）。
- 过滤条件下推到 SQL：分页查询的条件必须进 `WHERE`，不允许查全表再内存筛。
- 事务只加在 service 的"写聚合"方法（下单/支付/取消/发货），`rollbackFor = Exception.class`。
- 扣库存只用条件更新：`UPDATE product SET stock = stock - #{n} WHERE id = #{id} AND stock >= #{n}`，影响行数为 0 即库存不足。**禁止先 select 再判断再 update。**
- 状态迁移同样用条件更新：`WHERE id = ? AND status = ?`，影响行数为 0 视为状态已变更。
- 金额 `BigDecimal` + `decimal(10,2)`；时间 `LocalDateTime`；主键雪花 ID。

### 5.5 安全

- 所有"我的 XX"接口用 `SecurityUtil.currentUserId()`，**绝不接受前端传的 userId**。
- 密码 `BCryptPasswordEncoder` 存储；JWT 密钥不入库不入仓库。
- 用户输入展示需转义；富文本详情用白名单过滤。

---

## 6. 编码规范（前端）

- 组件文件 `PascalCase.vue`；组合式 API 统一 `<script setup>`；props 显式声明类型与默认值。
- `components/` 只放**无业务语义**的通用件；业务组件留在对应 `views/` 目录内。
- 请求只在 `api/` 目录里发，页面不直接 `axios`；统一走 `utils/request.js`。
- 每个列表页必须实现**四态**：loading / empty / error / 正常。只写正常态视为未完成。
- 业务计算（总价、库存判断、状态可流转性）**不在前端实现**，一律以后端返回为准。
- 金额按字符串展示，不做浮点运算；ID 按字符串处理，不 `parseInt`。
- 样式用 `<style scoped>`；配色与间距沿用现有页面，不引入新 UI 库。

---

## 7. API 约定（写代码前必读）

- 全局前缀 `/api`；三组：`/auth/**`、`/front/**`（游客+用户）、`/admin/**`（管理员）。
- 资源复数 + 动作子路径：`POST /front/orders/{id}/pay`、`PUT /admin/orders/{id}/ship`。
- 分页入参 `page`（1 起）/ `size`（≤100）；出参 `{records,total,current,size,pages}`。
- 错误码：`0` 成功｜`1xxx` 参数｜`2xxx` 认证授权（2001/2002/2003/2004）｜`3xxx` 业务（3001 库存不足、3002 状态不允许、3003 不存在、3004 重复操作、3005 已下架）｜`5000` 系统异常。
- 序列化三件套已全局配置，**不要逐字段加注解**：Long→String、`yyyy-MM-dd HH:mm:ss`、BigDecimal→String。
- 新增/修改接口必须先更新契约文档，再改代码（见 §10）。

---

## 8. 领域规则（不可自行更改）

| 规则 | 内容 | 出处 |
|---|---|---|
| 订单状态 | `0 待付款 → 1 待发货 → 2 待收货 → 3 已完成`，`0 → 4 已取消`；**只前进、无退款态**；5 条合法迁移 + 5 条禁止迁移 | `design/01` §1 |
| 超时取消 | 30 分钟未付款自动取消，`@Scheduled` 每 5 分钟扫表，**不引入 MQ** | `design/01` §1.5 |
| 商品规格 | **不做真 SKU**：`product.spec_json` 定义 + `cart/order_item.spec_text` 快照；价格库存仍按商品维度 | `design/01` §2 |
| 库存 | 单维 `product.stock`，**下单即扣**，取消/超时回补；条件更新防超卖 | `design/01` §3 |
| 订单快照 | `order_item` 必须存商品名、单价、主图、规格快照，禁止只存 product_id | `design/01` §1.6 |

需要变更上述任一条，**先停下来说明理由并取得用户确认**，不得在实现中"顺手改掉"。

---

## 9. Mock 规则

- Mock 由 `codes/frontend/mock/index.js`（自写 Vite 中间件）提供，**不使用 `vite-plugin-mock`**（其依赖 `bundle-require` 反复删临时文件，在本机安全策略下会被拦截）。
- Mock 数据必须与接口契约同构：字段名、类型、分页结构、错误码完全一致。
- 新增接口时同步加一条 mock 路由，保持"前端可独立跑通"。
- 切真接口只改 `.env.development` 的 `VITE_USE_MOCK=false`，**不改业务代码**。

---

## 10. AI 协作守则（核心）

### 10.1 每轮开工三件事

1. 读 `AGENTS.md` + 本轮涉及的契约条目（或 `design/` 中对应决策）。
2. 用一句话确认：**本次做什么、改哪些文件、验收标准是什么**。
3. 影响面超过 5 个文件时，先拆分，不要一口气改完。

### 10.2 一次只做一个可验收单元

- 单元 = 一个接口，或一个页面的一种状态。
- 反例："把商品模块做完"。正例："实现 `GET /admin/products` 分页查询：条件 name/categoryId/status，返回契约 PRD-01 结构，分页走 MyBatis-Plus"。
- 每完成一个单元立刻验证，不留未验证的代码。

### 10.3 强制验证（不通过不算完成）

| 改动 | 必须验证 |
|---|---|
| 后端接口 | `mvn -q compile` 通过 + 启动无异常 + 健康检查 200 + 该接口用 Apifox/curl 打通 |
| 前端页面 | `npm run build` 通过 + 页面四态可见 + 控制台无报错 |
| 数据库 | `schema.sql` 可重复执行不报错 + `SHOW CREATE TABLE` 与文档一致 |

### 10.4 禁止事项

- 禁止未说明就重命名字段/类/文件/路由。
- 禁止修改 `docs/` 下课程原件。
- 禁止修改 `design/00`–`design/02`（决策文档）而不说明理由。
- 禁止引入 §2.3 清单中的技术。
- 禁止"先改代码、后补契约"。
- 禁止吞异常、返回 null 表示失败、全表查询后内存过滤。
- 禁止在前端实现业务计算、在前端信任 userId。
- 禁止硬编码密钥、数据库密码、JWT secret 到 `.java`/`.vue`。

### 10.5 提交纪律

- 每完成一个可验收单元提交一次，message 格式：`feat(product): 商品分页查询接口`。
- 提交前确认 `git status` 不含 `target/`、`node_modules/`、`upload/`、`.env.local`。
- 改坏了直接回滚到上一个提交，不要让 AI"修回去"。

### 10.6 卡住时的处理

- 同一问题连续修 3 次仍失败：停止修改，说明"报错原文 + 已尝试方案 + 怀疑方向"，等用户决定。
- 不确定需求时先问，不要猜；猜错导致的大范围返工由 AI 负责拆分回滚。

---

## 11. 故障速查

| 现象 | 原因 | 处理 |
|---|---|---|
| 编译报 `invalid target release: 17` | 用了 JDK 11 | 切 `JAVA_HOME=E:\develop\jdk17` |
| 启动报 `Invalid value type for attribute 'factoryBeanObjectType'` | 用了 `mybatis-plus-boot-starter` | 换成 `mybatis-plus-spring-boot3-starter` |
| 时间差 8 小时 | JDBC URL 缺 `serverTimezone` | 补 `serverTimezone=Asia/Shanghai` |
| 列表返回全部数据、分页失效 | 未注册分页插件 | 检查 `MybatisPlusConfig` |
| 前端详情查不到数据 | 雪花 ID 精度丢失 | 确认 Long→String 全局配置 |
| 401 循环跳登录 | 拦截器未清 token / 登录接口被拦 | 检查 `SecurityConfig` 放行规则 |
| 图片 404 | 上传目录映射未生效 | 检查 `WebConfig` 与 `mall.upload.dir` |
| `docker: cannot connect` | Docker Desktop 未启动 | 先启动 Docker Desktop 至 Running |
| Redis 报 `NOAUTH` | 密码不一致 | `requirepass` 与 `REDIS_PASSWORD` 对齐 |

---

## 12. 文档索引

| 场景 | 先读 |
|---|---|
| 新一轮对话开场 | `design/00-project-baseline.md` |
| 改接口 / 写页面 | `design/03-dev-doc-plan.md` §2（契约模板）+ `design/02-architecture.md` §5 |
| 动订单 / 库存 / 规格 | `design/01-domain-decisions.md` |
| 配环境 / 排查依赖 | `design/00-env-inventory.md` + 本文件 §4、§11 |
| 写课程报告 | `design/03-dev-doc-plan.md` §4（章节映射表） |

---

**最后一条**：本项目的难度不在技术，在**契约一致性**与**改动可回滚**。任何一次"顺手优化"，都要先问自己是否破坏了这两点。
