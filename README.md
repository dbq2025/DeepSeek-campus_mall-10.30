# 校园电商平台 · Campus Mall

一个前后端分离的校园电子商务平台，包含**电商前台**（游客 / 学生用户）与**后台管理**（管理员）两个子系统。
项目由《前端开发技术课程设计》与《后端开发技术课程设计》两门课程共用一套接口契约与数据模型演进而来：
第一学期先以纯前端 + Mock 完成界面与交互，第二学期接入 Spring Boot 真实后端，实现完整业务闭环。

- 前台闭环：浏览 → 搜索筛选 → 商品详情 → 加入购物车 → 确认下单 → 模拟支付 → 订单查询 / 取消 / 收货
- 后台闭环：类型管理 → 商品管理（含规格编辑、图片上传）→ 订单管理（发货）→ 数据概览与统计

> 项目范围外（明确不做）：真实支付对接、物流轨迹、退款、优惠券、商品评价、真 SKU 库存。

---

## 一、主要功能

### 1.1 电商前台（`/`）

| 模块 | 功能 |
|---|---|
| 首页 | 首屏 Banner、分类入口、热销 / 新品商品卡片、空状态与骨架屏 |
| 商品列表 | 关键词搜索、分类筛选、排序（默认 / 价格升降 / 销量 / 最新）、分页 |
| 商品详情 | 主图、价格库存、规格选择（非 SKU，仅记录选择快照）、收藏、立即购买、加入购物车 |
| 购物车 | 列表与角标数量、修改数量（超库存拦截）、删除单行、清空、勾选结算 |
| 订单 | 确认订单（收货人信息校验）、创建订单、模拟支付、取消（回补库存）、确认收货、订单列表按状态筛选、订单详情与状态时间轴 |
| 收藏 | 收藏 / 取消收藏、收藏分页列表、详情页收藏态回显 |
| 账户 | 注册、登录、个人中心、退出登录；token 失效自动跳转并带回跳地址 |

### 1.2 后台管理（`/admin`）

| 模块 | 功能 |
|---|---|
| 数据概览 | 关键指标卡（今日订单、今日销售额、待发货、商品数等）+ 近 7 天销售额、订单状态分布、热销 TOP 5 |
| 类型管理 | 类型增删改查、排序 |
| 商品管理 | 分页检索、新增 / 编辑 / 上下架、逻辑删除、规格定义编辑（`spec_json`）、主图上传 |
| 订单管理 | 全量订单分页与状态筛选、订单详情、发货（填写运单号）、状态流转校验 |
| 数据统计 | 累计成交额 / 订单 / 待发货 / 在库商品与用户数、销售趋势、订单状态分布、热销商品排行（ECharts） |

### 1.3 演示账号

种子数据 `data.sql` 内置以下账号，密码统一为 `123456`：

| 账号 | 角色 | 用途 |
|---|---|---|
| `admin` | ADMIN | 后台管理端登录（`/admin/login`） |
| `test` | USER | 前台普通用户，含历史订单数据 |
| `lisi` | USER | 前台普通用户 |

---

## 二、技术栈

### 后端

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 必须 17，Spring Boot 3 不支持 JDK 8/11 |
| Spring Boot | 3.2.5 | Web / Validation / Security / Data Redis |
| MyBatis-Plus | 3.5.5 | 使用 `mybatis-plus-spring-boot3-starter` |
| Spring Security | 6.2.x | 无状态 JWT 鉴权，`SecurityFilterChain` 配置 |
| JWT | java-jwt 4.4.0（auth0） | 有效期 2 小时 |
| 数据库 | MySQL 8.0.34 | `campus_mall`，utf8mb4 |
| 连接池 | HikariCP | 最大 10 连接 |
| 工具 | Hutool 5.8.27 / Lombok | Lombok 仅编译期 |
| 构建 | Maven（单模块） | `finalName = campus-mall` |

### 前端

| 组件 | 版本 | 说明 |
|---|---|---|
| Vue | 3.4.x | 组合式 API + `<script setup>`，纯 JavaScript |
| Vite | 5.2.x | 开发服务器与构建，自写 Mock 中间件 |
| Element Plus | 2.7.x | 全量引入（换取零配置稳定） |
| Pinia | 2.1.x | 仅两个 store：`user`（登录态）、`cart`（角标） |
| Vue Router | 4.3.x | History 模式 + 全局守卫 |
| Axios | 1.7.x | 统一 `request.js`：注入 token、解包 `data`、业务码分流 |
| ECharts | 5.5.x | 按需引入 bar / line / pie |

### 基础设施

| 组件 | 说明 |
|---|---|
| Redis 7（Docker） | 商品详情 / 分类列表缓存；读失败自动降级查库，可整体切换为本地实现 |
| Docker Compose | 仅用于起 Redis，业务服务不容器化 |

---

## 三、目录结构

```
ZSpringBoot/
├── README.md                    ← 本文件
├── AGENTS.md                    ← AI 协作与编码规范（含技术选型、禁引入清单）
├── CLAUDE.md                    ← 指向 AGENTS.md
├── docker-compose.yml           ← 本地 Redis（mall-redis）
├── design/                      ← 设计决策与契约（事实来源）
│   ├── 00-project-baseline.md       项目基线与范围
│   ├── 00-env-inventory.md          环境实测清单
│   ├── 01-domain-decisions.md       订单状态机 / 规格 / 库存决策
│   ├── 02-architecture.md           架构、选型、接口规范
│   ├── 30-接口契约.md               44 个接口的唯一事实来源
│   ├── 40-页面与交互.md             页面与交互说明
│   ├── 43-视觉规范.md               配色、间距、组件视觉约定
│   └── 9x-进度看板.md               阶段计划与验收记录
├── docs/                        ← 课程原始答题纸（只读，勿改）
├── scripts/
│   ├── set-jdk17-global.ps1         全局切换 JDK 17（需管理员）
│   └── smoke-backend.mjs            44 个接口冒烟脚本
└── codes/
    ├── backend/                 ← Spring Boot 后端
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/gdufe/mall/
    │       │   ├── MallApplication.java
    │       │   ├── common/{result,exception,config,security,cache,util}
    │       │   ├── controller/        12 个 Controller（auth / front / admin / health）
    │       │   ├── service(+impl)/    业务编排、事务、状态与归属校验
    │       │   ├── mapper/            MyBatis-Plus BaseMapper
    │       │   ├── entity/ dto/ vo/
    │       │   └── task/              待付款超时取消定时任务
    │       └── resources/
    │           ├── application.yml        公共配置（端口、JWT、缓存、订单超时）
    │           ├── application-dev.yml    数据源、Redis、SQL 日志
    │           ├── db/schema.sql          建库建表（7 张表）
    │           ├── db/data.sql            幂等种子数据
    │           ├── static/files/          演示商品图（/files/p1.svg …）
    │           └── mapper/*.xml           多表 / 统计 SQL
    └── frontend/                ← Vue 3 前端
        ├── vite.config.js           代理 /api → 8080、/files → /api/files
        ├── .env.development / .env.production
        ├── mock/                    由契约派生的 Mock 中间件（db / routes / index）
        ├── scripts/                 shot.mjs 截图、smoke.mjs 前端冒烟
        └── src/
            ├── api/                 按模块拆：auth / product / cart / order / favorite / admin/*
            ├── components/          通用件：BaseTable / BaseDialog / BasePager /
            │                        ImageUpload / StatusTag / SpecSelector / EmptyState / ChartPanel
            ├── composables/         组合式复用逻辑
            ├── layout/              FrontLayout（顶栏 + 页脚）/ AdminLayout（侧栏 + 顶栏）
            ├── router/              index.js + front.routes / admin.routes / guard
            ├── stores/              user.js / cart.js
            ├── utils/               request.js / format.js / validate.js
            └── views/               front/*（11 页）、admin/*（8 页）、dev/Health.vue
```

---

## 四、环境要求

| 依赖 | 版本 / 说明 |
|---|---|
| JDK | **17**（如 `E:\develop\jdk17`），`java -version` 必须显示 17 |
| Maven | 3.9.x，建议在 `conf/settings.xml` 配好国内镜像与 `<localRepository>` |
| Node.js | ≥ 18（Vite 5 要求），包管理统一用 **npm** |
| MySQL | 8.0.x，本机 `3306`，需已启动 |
| Redis | 7.x，可选。用 Docker Compose 起，或让后端切本地缓存实现 |
| Docker Desktop | 仅当需要 Redis 时需要 |

---

## 五、快速开始

以下命令以项目根目录 `ZSpringBoot/` 为起点。

### 5.1 初始化数据库

`schema.sql` 自带 `CREATE DATABASE`，无需手工建库；`data.sql` 使用固定主键 + `ON DUPLICATE KEY UPDATE`，可重复执行。

```bash
# 建表（7 张：user / category / product / cart / orders / order_item / favorite）
mysql --default-character-set=utf8mb4 -uroot -p < codes/backend/src/main/resources/db/schema.sql

# 导入演示数据（含 3 个账号、5 个类型、8 个商品、历史订单）
mysql --default-character-set=utf8mb4 -uroot -p < codes/backend/src/main/resources/db/data.sql
```

> `--default-character-set=utf8mb4` 不能省：Windows 客户端默认 GBK，不加会因中文超长报 `Data too long`。

默认连接账号为 `root / 1234`（见 5.3 环境变量）。

### 5.2 启动 Redis（可选）

```bash
docker compose up -d
docker exec -it mall-redis redis-cli -a redis123 ping    # 期望 PONG
```

不启动 Redis 也能跑：把缓存实现切成本地即可（见 5.3）。

### 5.3 启动后端

```bash
# 关键：先确保 JAVA_HOME 指向 JDK 17
export JAVA_HOME=/e/develop/jdk17          # Git Bash
# Windows CMD：set JAVA_HOME=E:\develop\jdk17 且 set PATH=%JAVA_HOME%\bin;%PATH%

cd codes/backend
mvn clean package -DskipTests
mvn spring-boot:run
```

启动后自检（无需登录）：

```bash
curl http://localhost:8080/api/health
```

期望返回 `code = 0`，且 `dbOk = true`、`dbSchema = campus_mall`、`cacheOk = true`、`cacheType` 为 `redis` 或 `local`。

> 若 Redis 未启动，用 `MALL_CACHE_TYPE=local` 启动：`MALL_CACHE_TYPE=local mvn spring-boot:run`。

### 5.4 启动前端

```bash
cd codes/frontend
npm install
npm run dev
```

- 前台：<http://localhost:5173>
- 后台：<http://localhost:5173/admin>（用 `admin / 123456` 登录）
- 环境自检页：<http://localhost:5173/dev/health>

Vite 已配置代理：`/api → http://localhost:8080`，`/files → /api/files`，前端无需处理跨域。

---

## 六、环境变量说明

### 6.1 后端（`application.yml` / `application-dev.yml`）

全部采用 `${ENV:默认值}` 形式，未设置时使用括号内默认值。

| 变量 | 默认值 | 作用 |
|---|---|---|
| `MALL_JWT_SECRET` | 内置开发密钥 | JWT 签名密钥，**生产必须覆盖** |
| `MALL_CACHE_TYPE` | `redis` | 缓存实现：`redis` / `local` |
| `MALL_SERVER_PORT` | `8080` | 后端端口（也可用 `-Dserver.port=` 覆盖） |
| `MYSQL_USERNAME` | `root` | 数据库账号 |
| `MYSQL_PASSWORD` | `1234` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | `redis123` | Redis 密码 |

固定配置（不建议随意改）：`server.servlet.context-path=/api`、`mall.jwt.expire-hours=2`、`mall.upload.dir=./upload`、`mall.order.timeout-minutes=30`。图片上传仅接受 `jpg/jpeg/png/webp`，业务层限制 2MB（multipart 硬上限 5MB）。

> 启动类 `MallApplication` 会在未显式指定端口时把 `server.port` 固化为系统属性（默认 8080），避免宿主环境注入 `SERVER__PORT` 之类的变量导致端口漂移。要换端口请用 `-Dserver.port=xxxx` 或 `MALL_SERVER_PORT`。

### 6.2 前端（`.env.development` / `.env.production`）

| 变量 | 开发值 | 说明 |
|---|---|---|
| `VITE_API_BASE` | `/api` | Axios `baseURL` |
| `VITE_PROXY_TARGET` | `http://localhost:8080` | 开发代理目标 |
| `VITE_USE_MOCK` | `false` | `true` 走本地 Mock 中间件，`false` 走真实后端 |

打包后由同源 Nginx（或任意静态服务器）转发 `/api`，生产环境不再有 Vite 代理，因此 `VITE_API_BASE` 仍为 `/api`。

---

## 七、构建与部署

### 7.1 后端

```bash
cd codes/backend
mvn clean package -DskipTests           # 产物：target/campus-mall.jar
java -jar target/campus-mall.jar        # 默认 8080，profile=dev
```

生产运行示例：

```bash
MALL_JWT_SECRET='<随机长字符串>' \
MALL_CACHE_TYPE=redis \
MYSQL_PASSWORD='<生产密码>' \
java -jar campus-mall.jar --spring.profiles.active=prod
```

### 7.2 前端

```bash
cd codes/frontend
npm run build      # 产物：dist/
npm run preview    # 本地预览构建结果
```

部署时把 `dist/` 交给静态服务器，并将 `/api` 反向代理到后端 `8080`（需保留 `/api` 前缀，后端 context-path 为 `/api`）。上传图片目录 `codes/backend/upload` 通过 `/api/files/**` 访问，注意一并挂载持久化。

---

## 八、使用示例

### 8.1 一条完整下单链路（curl）

`BASE=http://localhost:8080/api`，以下路径均已包含 `/api`。

```bash
BASE=http://localhost:8080/api

# 1) 登录，取 token
curl -s -X POST "$BASE/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"123456"}'
# => {"code":0,"msg":"ok","data":{"token":"eyJ...","userInfo":{...}}}
TOKEN=<上一步返回的 token>

# 2) 浏览商品（支持 keyword / categoryId / sort / page / size）
curl -s "$BASE/front/products?keyword=耳机&sort=price_asc&page=1&size=10"

# 3) 商品详情
curl -s "$BASE/front/products/2001"

# 4) 加入购物车（有规格时必须传 specText）
curl -s -X POST "$BASE/front/cart" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"productId":"2001","quantity":2,"specText":"颜色:曜石黑"}'

# 5) 从购物车下单（fromCart=true + cartIds）
curl -s -X POST "$BASE/front/orders" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"fromCart":true,"cartIds":["<cartId>"],"receiverName":"小明","receiverPhone":"13900000000","receiverAddress":"广东财经大学 1 栋 101","remark":"尽快发货"}'
# => data: { orderId, orderNo, payAmount }

# 6) 模拟支付
curl -s -X POST "$BASE/front/orders/<orderId>/pay" -H "Authorization: Bearer $TOKEN"

# 7) 查询我的订单（status 可空 = 全部）
curl -s "$BASE/front/orders?status=1&page=1&size=10" -H "Authorization: Bearer $TOKEN"
```

立即购买则传 `fromCart=false` 与 `items`：

```bash
curl -s -X POST "$BASE/front/orders" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"fromCart":false,"items":[{"productId":"2001","quantity":1,"specText":"颜色:曜石黑"}],
       "receiverName":"小明","receiverPhone":"13900000000","receiverAddress":"广东财经大学 1 栋 101"}'
```

### 8.2 管理员端流程

```bash
# 管理员登录（非 ADMIN 角色返回业务码 2003）
curl -s -X POST "$BASE/auth/admin/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}'
ADMIN=<token>

# 新增商品
curl -s -X POST "$BASE/admin/products" \
  -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"categoryId":"1001","name":"测试商品","subtitle":"副标题","price":"99.00","stock":10,
       "specJson":"[{\"name\":\"颜色\",\"values\":[\"白\",\"黑\"]}]","detail":"<p>详情</p>"}'

# 上传主图（字段名固定 file，返回 /files/xxx.png）
curl -s -X POST "$BASE/admin/files" \
  -H "Authorization: Bearer $ADMIN" -F "file=@./demo.png"

# 发货
curl -s -X PUT "$BASE/admin/orders/<orderId>/ship" \
  -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"trackingNo":"SF1234567890"}'

# 数据统计
curl -s "$BASE/admin/stats/overview" -H "Authorization: Bearer $ADMIN"
```

### 8.3 接口冒烟脚本

后端启动后，可一次性验收 44 个接口与主要边界：

```bash
node scripts/smoke-backend.mjs
# 自定义地址：SMOKE_BASE=http://localhost:8080/api node scripts/smoke-backend.mjs
```

---

## 九、接口与数据约定

- 全局前缀 `/api`，三组路径：`/auth/**`（认证）、`/front/**`（游客 + 用户）、`/admin/**`（管理员）。
- 统一响应体 `{ code, msg, data }`：成功 `code = 0`；业务失败 HTTP 200 + `code ≠ 0`；认证失败 401、权限不足 403。
- 分页入参 `page`（1 起）/ `size`（≤100），出参 `{ records, total, current, size, pages }`。
- **ID 一律字符串**（雪花 ID 超出 JS 安全整数），**金额一律字符串**（两位小数），禁止前端浮点运算与 `parseInt`。
- 时间统一 `yyyy-MM-dd HH:mm:ss`（`Asia/Shanghai`），全局 Jackson 配置，无需逐字段注解。

错误码：

| code | 含义 | code | 含义 |
|---|---|---|---|
| 0 | 成功 | 3001 | 库存不足 |
| 1001 | 参数校验失败 | 3002 | 当前状态不允许该操作 |
| 1002 | 请求体格式错误 | 3003 | 资源不存在 |
| 2001 | 未登录 / 缺少令牌 | 3004 | 请勿重复操作 |
| 2002 | 令牌无效或已过期 | 3005 | 商品已下架 |
| 2003 | 无权限 | 5000 | 系统异常 |
| 2004 | 账号或密码错误 | | |

### 领域规则（实现前提）

- **订单状态机**：`0 待付款 → 1 待发货 → 2 待收货 → 3 已完成`，`0 → 4 已取消`；只前进、无退款态，状态迁移全部用条件更新（`WHERE id = ? AND status = ?`）保证幂等。
- **超时取消**：下单 30 分钟未支付，定时任务每 5 分钟扫表取消并回补库存（不引入 MQ）。
- **库存**：单维 `product.stock`，下单即扣，取消 / 超时回补；扣减只用 `WHERE stock >= n` 条件更新防超卖。
- **商品规格**：不做真 SKU。`product.spec_json` 定义可选项，`cart.spec_text` / `order_item.spec_text` 存选择快照，价格与库存仍按商品维度。
- **订单快照**：`order_item` 必须冗余商品名、单价、主图与规格文本，商品后续改价改名不影响历史订单。

数据库共 7 张表：`user`、`category`、`product`、`cart`、`orders`、`order_item`、`favorite`，建表语句见 `codes/backend/src/main/resources/db/schema.sql`。

---

## 十、设计文档

| 文档 | 内容 |
|---|---|
| `design/00-project-baseline.md` | 项目范围、业务闭环、协作规则 |
| `design/01-domain-decisions.md` | 订单状态机、规格方案、库存策略（含禁止迁移表） |
| `design/02-architecture.md` | 架构分层、技术选型理由、版本陷阱 |
| `design/30-接口契约.md` | **44 个接口的唯一事实来源**，改接口先改这里 |
| `design/40-页面与交互.md` | 页面清单、四态（loading/empty/error/正常）与交互细节 |
| `design/43-视觉规范.md` | 配色、字号、间距、组件视觉基线 |

---

## 十一、常见问题

**编译报「无效的目标发行版：17」/ `WebSecurityConfigurerAdapter` 找不到**
当前 shell 的 JDK 不是 17。执行 `java -version` 确认，并重新导出 `JAVA_HOME`（Windows 系统 PATH 常残留 JDK 11，可用 `scripts/set-jdk17-global.ps1` 以管理员身份彻底切换）。

**`/api/health` 返回 `dbOk: false`**
MySQL 未启动、库未初始化，或账号密码与环境变量不符。先用 5.1 的脚本建库导数。

**`cacheOk: false` 但不影响业务**
Redis 未启动。启动 `docker compose up -d`，或直接 `MALL_CACHE_TYPE=local` 切本地缓存（缓存异常本身也会降级查库）。

**导入 `data.sql` 报 `Data too long for column`**
客户端字符集不是 utf8mb4，命令中补 `--default-character-set=utf8mb4`。

**前端 401 后页面空白 / 一直被踢回登录**
token 已过期（2 小时）。重新登录即可；请求层会自动清理本地 token 并带 `redirect` 回跳。

**后台接口返回 403**
当前登录账号 `role != ADMIN`。后台必须用 `admin` 账号，且登录入口是 `/admin/login`（对应 `POST /auth/admin/login`）。

**商品图片 404**
开发环境 `/files/**` 依赖 Vite 代理重写到 `/api/files/**`；打包部署时需保证静态资源服务器同样把 `/files` 转发到后端，并确认 `codes/backend/upload` 与 `static/files` 可访问。

---

## 十二、协作与提交规范

- 编码规范、技术选型与禁止引入清单见 [`AGENTS.md`](./AGENTS.md)（唯一事实来源，`CLAUDE.md` 仅作导引）。
- 提交信息格式：`<type>(<scope>): <subject>`，例如 `feat(backend): 阶段5全量业务接口实现`、`fix(front): 修复购物车角标不刷新`。
- 一个可验收单元一次提交，保证可回滚；`target/`、`node_modules/`、`dist/`、`data/`、`.workbuddy/` 不入库。
