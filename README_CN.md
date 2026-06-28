# java-ddd-demo

> [English Docs](README.md)

Spring Boot 3.2 + DDD 六模块架构示例，包含两套仓储风格的对比实现。

## 架构图

```
                      ┌──────────────────────────────────────────────────────┐
                      │                  demo-interface                       │
                      │        REST Controller / 全局异常处理 / AOP          │
                      └──────────┬───────────────────────────────┬───────────┘
                                 │                               │
                          ┌──────▼──────┐                  ┌─────▼──────┐
                          │  demo-start  │                  │    DTO     │
                          │   启动类      │                  │  Command   │
                          │   配置        │                  │  Query     │
                          └──────┬───────┘                  │  CO / Resp │
                                 │                          └─────┬──────┘
                                 │                                │
                      ┌──────────▼────────────────────────────────▼──────────┐
                      │                  demo-application                     │
                      │          AppService（编排层，不含业务逻辑）            │
                      │          Converter（DTO ↔ 领域对象）                  │
                      └──────────┬───────────────────────────────┬───────────┘
                                 │                               │
                    ┌────────────▼────────────┐      ┌───────────▼───────────┐
                    │     demo-domain          │      │   demo-infrastructure │
                    │                          │      │                       │
                    │  聚合根 (Aggregate Root)  │      │  JPA 实现 / PO        │
                    │  值对象 (Value Object)    │      │  消息队列生产者        │
                    │  领域服务 (Domain Service)│◄─────│  外部 API 客户端       │
                    │  仓储接口 (Repository)    │      │                       │
                    │  领域事件 (Domain Event)  │      └───────────────────────┘
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     demo-common          │
                    │  业务异常 / 错误码        │
                    │  日志注解 / 事件基类      │
                    └──────────────────────────┘
```

### 依赖方向

```
interface → application → domain ← infrastructure
                              ↑
                          common（所有模块共享）
```

## 技术栈

- Java 21 / Spring Boot 3.2.3
- Spring Data JPA (Hibernate 6.4)
- PostgreSQL
- JUnit 5 + AssertJ

## 快速开始

```bash
# 1. 启动 PostgreSQL 并创建数据库
createdb ddd_demo

# 2. 如需修改数据库连接
#    demo-start/src/main/resources/application.yml

# 3. 启动
./mvnw spring-boot:run -pl demo-start

# 4. 测试
./mvnw test
```

## 模块概览

| 模块 | 层 | 职责 |
|------|-----|------|
| `demo-common` | 公共层 | 异常、事件基类、日志注解 |
| `demo-domain` | 领域层 | 聚合根、值对象、仓储接口、领域服务 |
| `demo-application` | 应用层 | 用例编排、DTO、转换器 |
| `demo-infrastructure` | 基础设施层 | JPA 实现、消息队列、外部服务 |
| `demo-interface` | 接口层 | REST Controller、全局异常处理 |
| `demo-start` | 启动层 | 启动类、配置、数据库建表 |

## API 接口

### Skill（风格一：PO/Domain 分离）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/skills` | 创建技能 |
| GET | `/api/skills/{id}` | 查询单个 |
| GET | `/api/skills` | 分页列表 |
| PUT | `/api/skills/{id}` | 更新详情 |
| PUT | `/api/skills/{id}/name` | 修改名称 |
| PUT | `/api/skills/{id}/status` | 状态变更 |
| POST | `/api/skills/{id}/activate` | 启用 |
| POST | `/api/skills/{id}/archive` | 归档 |
| POST | `/api/skills/batch-archive` | 批量归档 |
| DELETE | `/api/skills/{id}` | 删除 |

### Project（项目）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/projects` | 创建项目 |
| GET | `/api/projects/{id}` | 查询单个 |
| GET | `/api/projects` | 查询全部 |
| PUT | `/api/projects/{id}` | 更新 |
| POST | `/api/projects/{id}/activate` | 激活 |
| POST | `/api/projects/{id}/complete` | 完成 |
| DELETE | `/api/projects/{id}` | 删除 |

### Order（订单）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/{id}` | 查询单个 |
| POST | `/api/orders/{id}/pay` | 支付 |
| DELETE | `/api/orders/{id}` | 删除 |

## 仓储风格对比

详见：[docs/ddd-repository-styles.md](docs/ddd-repository-styles.md)

| | 风格一 (Skill) | 风格二 (Skill2) |
|---|---|---|
| 领域模型 | 纯 POJO | @Entity 贴领域对象 |
| PO 类 | 有 | 无 |
| Converter | 有（toDomain / toPO） | 无 |
| Repository 实现 | 手写 Impl 类 | JpaRepository 自动生成 |
| 精准字段更新 | Native SQL | JPQL @Modifying |
| 额外文件数 | 6 个 | 0 个 |

## DDD 核心实践

| 实践 | 说明 |
|------|------|
| 充血模型 | 行为方法在聚合根内（`activate()` / `pay()` / `archive()`），不散落 Service |
| 工厂方法 | `register()` / `place()` / `initiate()` 替代 new + setter |
| reconstitute() | 从持久层重建聚合根，转换器不调业务构造器 |
| 领域事件 | 状态变更触发事件，事务提交后发布 |
| CQRS | 读仓库返回轻量投影，写仓库操作聚合根 |
| 状态机 | 枚举 + switch 校验合法路径 |
