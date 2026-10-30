# 开工前检查清单（Phase 0 Preflight）

> 执行时间：2026-10-30 19:45｜结论：**除 JDK 全局切换需你执行一次管理员脚本外，其余全部就绪**
> 每天开工前扫一眼本文 §1 的表格，五项全绿即可开工。

---

## 1. 环境矩阵（逐项可验证）

| # | 项 | 值 | 验证命令 | 期望 |
|---|---|---|---|---|
| 1 | JDK（项目用） | 17.0.19 @ `E:\develop\jdk17` | `mvn -v` | `Java version: 17.0.19` |
| 2 | Maven | 3.9.4，仓库 `E:\develop\apache-maven-3.9.4\mvn_repo` | `mvn -v` | 显示 Maven home 与上述仓库 |
| 3 | Node / npm | 22.22.2 / 11.16.0 | `node -v` `npm -v` | v22.x / 11.x |
| 4 | MySQL | 8.0.34，3306，`root/1234`，库 `campus_mall` | `mysql -uroot -p1234 -e "show tables from campus_mall;"` | 7 张表 |
| 5 | Redis | 容器 `mall-redis`，6379，密码 `redis123` | `docker exec mall-redis redis-cli -a redis123 ping` | `PONG` |
| 6 | 后端 | 8080，`/api` | `curl http://localhost:8080/api/health` | `code=0, dbOk=true` |
| 7 | 前端 | 5173 | 浏览器打开 | 环境自检页 |
| 8 | Git | main 已关联 origin | `git status -sb` | `## main...origin/main` |

后端启动：

```bat
set JAVA_HOME=E:\develop\jdk17
cd codes\backend && mvn spring-boot:run
```

前端启动：`cd codes\frontend && npm run dev`

---

## 2. JDK 17 全局默认：做了什么、还差一步

**已完成（用户级，立即写入注册表 `HKCU\Environment`）**

- `JAVA_HOME = E:\develop\jdk17`
- `Path` 首位加入 `E:\develop\jdk17\bin`

**为什么还不够**：Windows 拼接 PATH 的顺序是「系统 PATH → 用户 PATH」，而系统 PATH 里 `E:\develop\jdk11\bin` 排在最前，所以 `where java` 仍命中 JDK 11。改系统 PATH 需要管理员权限，agent 当前非管理员，无法直接完成。

**还差的一步（一次性）**：右键 `scripts\set-jdk17-global.ps1` → 以管理员身份运行。脚本会：备份现有值到 `scripts\env-backup.txt` → 把系统 `JAVA_HOME` 设为 jdk17 → 把系统 PATH 中 `jdk11\bin` 原位替换为 `jdk17\bin` → 广播环境变更。

**生效方式**

| 场景 | 是否立即生效 |
|---|---|
| Maven / Gradle / IDEA 新开构建 | ✅ 读 `JAVA_HOME`，已生效 |
| 已打开的 CMD / PowerShell / 终端 | ❌ 需关掉重开 |
| 已运行的 IDEA（改 JDK 需在 Project Structure 里选 17） | ❌ 需重启或手动指定 |
| 新建终端 / 重启后 | ✅ |

**验证三连**（新开一个 CMD 执行）

```bat
where java          :: 期望第一行是 E:\develop\jdk17\bin\java.exe
java -version       :: 期望 17.0.19
mvn -v              :: 期望 Java version: 17.0.19
```

> 若暂时不想动系统环境：项目内一切构建已通过 `JAVA_HOME` 指向 17 完成，不影响开发，只是裸 `java` 命令仍是 11。

---

## 3. Redis（Docker）实测结果

| 项 | 结果 |
|---|---|
| 引擎 | Docker 28.5.1 |
| 镜像 | `redis:7-alpine`，ID `8da19c7399dc`，digest `sha256:8da19c73…54d3f`，构建于 2026-10-26 |
| 服务端版本 | `Redis server v=7.4.12`（jemalloc 5.3.0, 64-bit） |
| 容器 | `mall-redis`，`Up (healthy)`，`0.0.0.0:6379->6379` |
| 重启策略 | `unless-stopped`（开机/重启 Docker 后自动拉起） |
| 持久化 | AOF 已开启，落盘 `./data/redis/appendonlydir` |
| 认证 | `requirepass redis123`，`PING` 返回 `PONG` |
| 应用联调 | 后端以 `--mall.cache.type=redis` 启动，`/api/health` 返回 `cacheType=redis, cacheOk=true` ✅ |

**注意**：应用默认仍是 `mall.cache.type=local`，目的是「Docker 没起也能开发」。要做缓存相关演示/报告章节时，把该值改为 `redis` 重启即可。

---

## 4. Git：已完成配置与提交规范

**已完成**

- `git init -b main`，remote `origin` = `https://github.com/dbq2025/DeepSeek-campus_mall-10.30.git`
- 首次提交 `e3bd3a6`（52 个文件），已推送，`main` 跟踪 `origin/main`
- `credential.helper = manager`（GCM 已有 GitHub 凭据，推送无需再登录）
- 仅 `github.com` 走代理 `127.0.0.1:7897`

**被忽略的目录**：`target/`、`node_modules/`、`dist/`、`data/`、`upload/`、`.workbuddy/`、`.idea/`、`application-prod.yml`、`.env.local`。

**提交规范**

```
<type>(<scope>): <subject>

type:  feat | fix | docs | refactor | test | chore
scope: front | back | db | docs | build
例：feat(back): 商品分页查询接口
    feat(front): 商品列表页四态与筛选
    fix(back): 下单库存条件更新返回值判断
```

- 一个可验收单元一次提交；提交前必须验证通过（编译/构建/接口 200）。
- 流程：`git pull --rebase` → 改 → 验证 → `git add -A` → `git commit` → `git push`。
- 分支：`main` 只放可运行版本；试验性改造用 `feat/front-xxx`、`feat/back-xxx`，合回 main 前先自测。
- 禁止：`push -f` 到 main、提交上述被忽略目录、提交数据库密码到非 `application-dev.yml` 的文件。

---

## 5. 明天双窗口并行开发：必须约定的事

两个窗口 = 两个互不可见的 AI 会话，**它们唯一的共享内存是磁盘上的文件**。下面六条是避免互相踩踏的关键。

### 5.1 每个窗口的开场三件套

1. 贴 `design/00-project-baseline.md` 全文（项目坐标 + 规则）
2. 声明身份与边界：`我是【前端窗口】，只改 codes/frontend/**`
3. 声明本次任务 + 验收标准 + 允许改动的文件

> 不要指望 AI 自己想起另一个窗口在做什么——它不会。

### 5.2 文件所有权（最重要，冲突 90% 来自这里）

| 目录/文件 | 归属 | 另一方 |
|---|---|---|
| `codes/frontend/**` | 前端窗口独占 | 后端窗口只读 |
| `codes/backend/**` | 后端窗口独占 | 前端窗口只读 |
| `design/30-接口契约.md` | **契约窗口**（建议由前端窗口兼） | 只读，需变更走申请 |
| `AGENTS.md`、`docker-compose.yml`、`.gitignore` | 你本人 | 两个窗口都不改 |
| `codes/backend/.../db/schema.sql` | 后端窗口 | 前端窗口只读 |

### 5.3 契约先行 + 冻结

- 开工前先花 1 小时把 `design/30-接口契约.md` 写到「认证 + 商品 + 购物车」三组（约 15 条），打 `v1.0` 标签并提交。
- 前端 mock 严格照契约造数据；后端照契约实现 controller。
- **变更流程**：提出 → 评估影响面（页面/mock/接口/用例）→ 一次改完 → 版本 +0.1 → commit → 另一窗口 `git pull` 后再动。禁止"代码先改、文档后补"。

### 5.4 依赖与版本冻结

- 两个窗口**都不许**改 `pom.xml` 的依赖版本、`package.json` 的依赖版本。要加依赖先问你，一次只加一个，加完立刻 commit 并通知另一窗口 `git pull` + 重新安装（`npm install` / `mvn -U`）。
- 版本已定版：Boot 3.2.5 / MP 3.5.5 / Vue 3.4 / Vite 5.2 / Element Plus 2.7 / java-jwt 4.4.0。**不要**让 AI "顺手升级到最新"。

### 5.5 环境一致性

| 共享资源 | 风险 | 约定 |
|---|---|---|
| MySQL `campus_mall` | 两边造数据互相污染，前端看到"莫名多出的商品" | 前端只用 mock；需要真数据时用固定测试账号 `test01`，后端造数统一放 `data.sql` |
| Redis db 0 | key 撞车 | 业务 key 统一前缀 `mall:` |
| 端口 8080 | 起两个后端实例直接失败 | 同一时间只跑一个后端；改端口用 `-Dserver.port` |
| 端口 5173 | 第二个 Vite 自动跳 5174 | 前端窗口固定 5173，冲突时先关旧进程 |

### 5.6 Git 并发：必须串行提交

- 两个终端**不能同时** `git commit`（会抢 `.git/index.lock`，报 `Unable to create index.lock`）。
- 约定节奏：写完 → 验证 → 提交 → 推 → 告诉另一窗口可以 pull。
- 提交前统一 `git pull --rebase`；出现冲突时**由改共享文件的一方解决**，不要两边各改一半。

### 5.7 三个"假 bug"，别浪费时间去查

1. 前端页面数据是 mock 的 → 改后端接口不生效。先看 `.env.development` 的 `VITE_USE_MOCK`。
2. 后端改了代码但没重启 → 8080 上跑的还是旧 jar。
3. 两个窗口看到的文件内容不一致 → 一方没 `pull`，或改在未保存的编辑器缓冲里。

---

## 6. 建议

1. **先契约，后页面**。前端窗口第一小时不要写页面，先把 15 条契约 + mock 骨架落地，后面能省掉一轮返工。
2. **后端窗口先做认证闭环**（注册/登录/JWT 过滤器/权限规则），因为所有需要归属校验的接口都依赖它。
3. **每天收工前**：`git push` + 在 `design/90-进度看板.md` 写三行（今天完成/卡点/明天第一件事）。第二天两个窗口都从看板恢复上下文，比读聊天记录快。
4. **Docker 随开机启动**（Docker Desktop 设置里勾选 Start Docker Desktop when you sign in），否则每天第一件事是等它转圈。
5. 答辩素材（截图、测试表）从第一天开始攒，不要最后补。
