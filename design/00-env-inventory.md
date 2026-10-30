# 阶段 0 · 本机环境实测清单（v2 · 已落地）

> 首次实测 2026-10-30 18:56｜落地复核 2026-10-30 19:30｜方式：命令行探测（非管理员）
> 状态标记：✅ 已就绪　⚠️ 可用但有前提　⛔ 未完成

## 一、环境实测与落地状态

| 类别 | 组件 | 实测值 | 状态 |
|---|---|---|---|
| 运行时 | JDK（PATH 默认） | OpenJDK 11.0.32，`JAVA_HOME=E:\develop\jdk11` | ⚠️ 不是本项目用的版本 |
| 运行时 | JDK 17 | OpenJDK 17.0.19 LTS，`E:\develop\jdk17` | ✅ 后端已用它构建成功 |
| 构建 | Maven | 3.9.4，`E:\develop\apache-maven-3.9.4` | ✅ |
| 构建 | Maven 本地仓库 | `E:\develop\apache-maven-3.9.4\mvn_repo`（settings.xml 已配） | ✅ |
| 构建 | Maven 镜像 | `conf/settings.xml` 已存在 `alimaven`（`http://maven.aliyun.com/...`，mirrorOf=central） | ✅ 沿用，勿改 |
| 构建 | 依赖下载 | 通过代理可正常拉取，首次构建 1 分 20 秒完成 | ✅ |
| 前端 | Node.js / npm | v22.22.2 / 11.16.0，registry=`registry.npmmirror.com` | ✅ |
| 前端 | 依赖安装 | `npm install` 86 包 / 1 分 17 秒；esbuild 的 postinstall 被跳过但可用 | ✅ 构建通过 |
| 前端 | pnpm / yarn | 未安装 | ⚠️ 统一用 npm |
| 数据库 | MySQL | **8.0.34**，3306 / 33060 LISTENING（PID 19512） | ✅ 运行中 |
| 数据库 | 账号 | `root` / `1234` | ✅ 已验证 |
| 数据库 | 项目库 | `campus_mall`，7 张表（`user` `category` `product` `cart` `orders` `order_item` `favorite`），69 个字段 | ✅ 已建好 |
| 缓存 | Docker Desktop | 已安装（`C:\Program Files\Docker\Docker`），但 **agent 无法保持其进程存活**；引擎管道 `dockerDesktopLinuxEngine` 不存在 | ⛔ 需人工双击启动 |
| 缓存 | Redis 容器 | `docker-compose.yml` 已就绪（`redis:7-alpine`，6379，密码 `redis123`，AOF 持久化），`docker compose config` 校验通过 | ⛔ 待 Docker 启动后 `docker compose up -d` |
| 缓存 | 应用侧 | `mall.cache.type=local`（默认，无需中间件即可跑通）；切 `redis` 只改一行 | ✅ 可降级 |
| 版本控制 | Git | 2.55.0.windows.3 | ✅ |
| 端口 | 8080 / 5173 | 均空闲（8080 已由后端占用验证通过） | ✅ |

## 二、实测发现的两个坑（已修复，写入 AGENTS.md）

1. **宿主注入了 `SERVER__PORT=62497`**，被 Spring 宽松绑定成 `server.port`，导致首次启动报 "Port 62497 was already in use"。
   已在 `MallApplication.main` 用系统属性固定端口（优先级高于环境变量），可用 `-Dserver.port` 或 `MALL_SERVER_PORT` 覆盖。
2. **Spring 自动生成内存用户**（日志里出现 `Using generated security password`），说明鉴权链走到了默认配置。
   已排除 `UserDetailsServiceAutoConfiguration`，后续登录完全由 JWT 负责。

## 三、四条关键结论

1. **必须显式切 JDK 17**：PATH 默认是 11，Boot 3.2 编译会直接失败。构建与启动前 `set JAVA_HOME=E:\develop\jdk17`。
2. **Maven 侧无需任何改动**：本地仓库与 alimaven 镜像都已配好，首次构建已验证通过（耗时 80 秒）。
3. **数据库已可用**：`root/1234` + `campus_mall` + 7 表 + 69 字段，后端 `/api/health` 已实测返回 `dbOk=true`。
4. **Redis 是唯一未完成项，且不阻塞开发**：Docker Desktop 需要人工双击启动（agent 拉起的进程会被沙箱回收）；默认走本地缓存实现，业务代码零改动。Docker 起来后执行 `docker compose up -d`，再把 `mall.cache.type` 改为 `redis` 即可。

## 四、启动顺序（明天照这个来）

1. 双击 Docker Desktop，等托盘图标变为 Running（可选，只影响 Redis）
2. `docker compose up -d`（可选）
3. 后端：`set JAVA_HOME=E:\develop\jdk17` → `cd codes\backend` → `mvn spring-boot:run`
4. 前端：`cd codes\frontend` → `npm run dev`
5. 浏览器打开 `http://localhost:5173`，首页即环境自检页
