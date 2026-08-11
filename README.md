# 学习项目 README

## 项目结构

```
overseas-learning/
├── pom.xml                                              # Maven 配置
├── src/main/java/com/overseas/learning/
│   ├── OverseasLearningApplication.java                 # 启动类
│   ├── common/                                          # 公共基础类
│   │   ├── Result.java                                  # 统一返回结果
│   │   ├── ResultCode.java                              # 错误码枚举
│   │   ├── BusinessException.java                       # 业务异常
│   │   └── GlobalExceptionHandler.java                  # 全局异常处理
│   ├── config/                                          # 配置类（预留）
│   ├── controller/                                      # Controller 层（路由 + 参数校验）
│   │   ├── MerchantController.java                      # 商户 CRUD
│   │   └── OnboardingController.java                    # 进件管理
│   ├── biz/                                             # Biz 业务层（业务逻辑编排，★ 重点）
│   │   ├── BizMerchantService.java                      # 商户业务接口
│   │   ├── BizOnboardingService.java                    # 进件业务接口
│   │   └── impl/
│   │       ├── BizMerchantServiceImpl.java              # 商户业务实现
│   │       └── BizOnboardingServiceImpl.java            # 进件业务实现
│   ├── service/                                         # Service 数据层（单表 CRUD）
│   │   ├── MerchantService.java
│   │   ├── OnboardingService.java
│   │   └── impl/
│   │       ├── MerchantServiceImpl.java
│   │       └── OnboardingServiceImpl.java
│   ├── dao/                                             # DAO（Mapper 接口）
│   │   ├── MerchantMapper.java
│   │   └── OnboardingRecordMapper.java
│   ├── entity/                                          # 实体类
│   │   ├── Merchant.java
│   │   ├── OnboardingRecord.java
│   │   └── TradeOrder.java
│   └── dto/                                             # DTO（数据传输对象）
│       ├── MerchantCreateDto.java
│       ├── MerchantQueryDto.java
│       ├── OnboardingSubmitDto.java
│       └── PageResult.java
├── src/main/resources/
│   ├── application.yml                                  # 主配置
│   ├── application-dev.yml                              # H2 配置
│   ├── application-mysql.yml                            # MySQL 配置
│   ├── db/
│   │   ├── schema.sql                                   # H2 建表
│   │   ├── schema-mysql.sql                             # MySQL 建表
│   │   └── data.sql                                     # 测试数据
│   ├── mapper/
│   │   ├── MerchantMapper.xml                           # MyBatis XML
│   │   └── OnboardingRecordMapper.xml
│   └── static/
│       └── index.html                                   # 前端测试页面
└── src/test/java/                                       # 测试（预留）
```

---

## 一、启动项目

### 1.1 前提条件
- Java 1.8（已安装: `D:\javasdk\jre`）
- Maven 3.6+（已安装: `D:\java-operation\apache-maven-3.6.3`）
- MySQL 8.0.28（已安装，root/root，数据库 `oversea_learning`）

### 1.2 启动

```bash
# 在 E:\overseas\overseas-learning 目录下执行
mvn spring-boot:run
```

或者用 IDE（IntelliJ IDEA）导入 Maven 项目后运行 `OverseasLearningApplication.main()`。

### 1.3 验证
- 浏览器访问: http://localhost:8080 （前端测试页面）
- 接口测试: http://localhost:8080/api/merchants
- H2 Console（如果用的 H2）: http://localhost:8080/h2-console

---

## 二、框架干活指南

### 2.1 分层架构（从请求到响应的完整链路）

```
浏览器请求
    ↓
[Controller]            ← 路由 + 参数校验（@Valid），不写业务逻辑
    ↓
[Biz 层]  BizXxxService  ← ★ 业务逻辑编排：业务校验、DTO转换、事务、组合多个数据层、调外部接口
    ↓
[Service 层] XxxService  ← 数据层：只封装单表 CRUD，不写业务判断
    ↓
[DAO / Mapper]          ← MyBatis SQL 映射
    ↓
[MySQL / H2]            ← 数据库
```

#### 为什么要有 Biz 层？

这是企业项目（如 qingo）和简单 demo 的核心区别。判断一段逻辑放哪层：

| 逻辑类型 | 放哪层 | 例子 |
|---------|--------|------|
| 业务规则校验 | **Biz 层** | 「邮箱不能重复」「审核中才能回调」 |
| DTO ↔ Entity 转换 | **Biz 层** | `BeanUtils.copyProperties(dto, entity)` |
| 跨表/跨模块组合调用 | **Biz 层** | 进件时先查商户状态，再写进件表 |
| 事务边界 `@Transactional` | **Biz 层** | 多步写操作要么全成功要么全回滚 |
| 单表增删改查 | **Service 数据层** | `selectById` / `insert` / `update` |
| SQL 语句 | **Mapper/XML** | `SELECT * FROM merchant WHERE ...` |

一句话：**Biz 层关心「业务流程对不对」，数据层关心「数据怎么存取」。**

#### 真实项目（qingo）对照

```
学习项目                          qingo 真实项目
─────────────────────────────────────────────────────────
MerchantController               GoodsBrandController
    ↓                                ↓
BizMerchantService               BizGoodsBrandService   ← 都以 Biz 开头
    ↓                                ↓
MerchantService (数据层)          svem_biz_orm_common 里的 XxxService
    ↓                                ↓
MerchantMapper (自己写XML)        公共 ORM 模块，较少手写 SQL
```

差异说明：qingo 是多模块 Maven 工程，数据层 Service 和实体类抽到了公共模块
`svem_biz_orm_common` 里供所有子模块复用；学习项目是单模块，所以数据层就在本地。
但「Biz 层编排业务、数据层只管 CRUD」这个分层思想完全一致。

### 2.2 一个完整的请求流程

以「创建商户」为例：

```
POST /api/merchants  { fullName: "xxx", bizType: 1, ... }
    │
    ▼
MerchantController.create()                    ← 只接参数、@Valid 校验
    │ 校验失败 → GlobalExceptionHandler → 返回 1001
    ▼
BizMerchantServiceImpl.create()                ← ★ 业务层（@Transactional）
    │ 1. 校验业务规则：邮箱是否已存在
    │ 2. DTO → Entity 转换
    │ 3. 设置默认值（currency/country/status）
    ▼
MerchantServiceImpl.save()                     ← 数据层（纯写入）
    ▼
MerchantMapper.insert()                        ← MyBatis XML → INSERT INTO merchant ...
    ▼
返回 Result.success(merchant)
```

再看「提交进件」（更能体现 Biz 层价值，跨了商户 + 进件两个数据层）：

```
POST /api/onboarding  { merchantId: 1, receiveType: 1, ... }
    │
    ▼
OnboardingController.submit()
    ▼
BizOnboardingServiceImpl.submit()              ← ★ 业务层（@Transactional）
    │ 1. 调 MerchantService 校验商户存在且未禁用   ← 跨数据层
    │ 2. 调 OnboardingService 查是否已有审核中的进件（防重复）
    │ 3. 调 OnboardingService 保存草稿
    │ 4. 模拟调用支付中台 → 拿 applyNo
    │ 5. 调 OnboardingService 更新状态为审核中
    │ → 任何一步失败，整个事务回滚
    ▼
返回 Result.success(record)
```

### 2.3 关键注解速查

| 注解 | 作用 | 在项目中的使用 |
|------|------|--------------|
| `@RestController` | 声明这个类是 Controller，返回 JSON | 所有 Controller |
| `@RequestMapping` | 路由前缀 | 类级别统一路径 |
| `@GetMapping/POST/PUT/DELETE` | HTTP 方法映射 | 方法级别 |
| `@PathVariable` | URL 路径参数 `/api/merchants/{id}` | 获取 ID |
| `@RequestParam` | URL 查询参数 `?name=xxx` | 查询条件 |
| `@RequestBody` | 请求体 JSON 转 Java 对象 | 创建/更新 |
| `@Valid` | 触发参数校验 | 校验 DTO |
| `@Service` | 注册为 Service Bean | 所有 Service 实现 |
| `@Transactional` | 事务管理（失败回滚） | 写操作 |
| `@Mapper` | 注册为 MyBatis Mapper | 所有 DAO 接口 |
| `@Autowired` / `@RequiredArgsConstructor` | 依赖注入 | 注入 Service/Mapper |
| `@Slf4j` | 日志 | 所有类记录日志 |
| `@ExceptionHandler` | 异常处理 | 统一异常处理 |

### 2.4 项目中的命名规范

| 项目 | 含义 | 示例 |
|------|------|------|
| `Biz*Service` | 业务 Service | `BizMctRegisterService` |
| `*Controller` | Controller | `MctRegisterController` |
| `*Mapper` | DAO 接口 | `MerchantMapper` |
| `*Dto` | 数据传输对象 | `MctRegisterDto` |
| `*ResDto` | 响应 DTO | `MctRegisterResDto` |
| `*Entity` | 实体 | `Mct` |
| `*ServiceImpl` | Service 实现 | `MerchantServiceImpl` |
| `*Client` | 外部服务调用 | `OnboardingClient` |

### 2.5 统一返回结果

```java
// 成功
Result.success(data)          → { code: 0, msg: "success", data: {...} }
Result.success()              → { code: 0, msg: "success", data: null }

// 失败
Result.error(ResultCode.PARAM_ERROR)  → { code: 1001, msg: "参数错误", data: null }
```

### 2.6 抛业务异常

```java
// 在 Service 中直接抛，Controller 不用 try-catch
throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
// 或自定义消息
throw new BusinessException(1001, "邮箱已被注册");
```

### 2.7 分页查询（PageHelper）

```java
// 在 Service 中查询前调用
PageHelper.startPage(page, size);

// 随后的第一个 SQL 会自动拼接 LIMIT
List<Merchant> list = merchantMapper.selectList(condition);

// 获取总数
long total = ((Page<Merchant>) list).getTotal();
```

---

### 2.8 DTO → Entity 转换：三种方式对比（★ 本项目用 Builder）

实体类上这几个注解是配套的，缺一不可：

```java
@Data                  // getter/setter/toString
@Builder               // 支持 XxxEntity.builder().field(v).build() 链式构建
@NoArgsConstructor     // 无参构造 —— MyBatis 反射创建对象必须用
@AllArgsConstructor    // 全参构造 —— @Builder 内部依赖它
public class Merchant { ... }
```

> ⚠️ 坑：只加 `@Builder` 不加 `@AllArgsConstructor` 会编译报错；
> 加了 `@Builder`/`@AllArgsConstructor` 但没加 `@NoArgsConstructor`，
> MyBatis 查库时会报「无参构造不存在」。所以这三个要一起加。

#### 三种转换方式对比

```java
// ===== 方式1：BeanUtils.copyProperties（简单，示例项目改造前用的）=====
Merchant m = new Merchant();
BeanUtils.copyProperties(dto, m);   // 反射批量拷同名字段
m.setStatus(1);                      // 差异字段手动补
// 优点：一行省事   缺点：反射慢、字段名写错不报错（静默不拷）、null 照拷

// ===== 方式2：Lombok @Builder（★ qingo 真实项目在用，本项目采用）=====
Merchant m = Merchant.builder()
        .fullName(dto.getFullName())
        .bizType(dto.getBizType())
        .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")  // 顺手做默认值
        .status(1)                                                        // 顺手做状态
        .build();
// 优点：每个字段来历清晰、无反射、编译期检查、可在链式里做加工
// 缺点：字段多时要写一长串

// ===== 方式3：MapStruct（行业高性能方案，本项目/qingo 都没用）=====
@Mapper
public interface MerchantConverter {
    Merchant toEntity(MerchantCreateDto dto);   // 编译期自动生成实现类
}
// 优点：编译期生成代码、最快、写错字段编译报错
// 缺点：要加依赖 + 配置注解处理器，小项目属于过度设计
```

#### 怎么选

| 场景 | 推荐 |
|------|------|
| 学习/demo，快速跑通 | `BeanUtils.copyProperties` |
| **企业项目（如 qingo），字段需精确控制/加工** | **`@Builder`** |
| 转换极频繁、追求极致性能 | MapStruct |

**结论**：本项目已对齐 qingo，统一用 `@Builder`。你在 qingo 代码里会看到大量
`XxxEntity.builder().xxx(v).build()`，和这里写法完全一致。

---

## 三、业务场景演练

### 3.1 商户管理 CRUD

```
创建: POST   /api/merchants        → { fullName, bizType, email, ... }
查询: GET    /api/merchants/{id}   → 单个商户
列表: GET    /api/merchants        → 分页列表（?page=1&size=20&fullName=xxx）
更新: PUT    /api/merchants/{id}   → { fullName, bizType, ... }
删除: DELETE /api/merchants/{id}   → 删除
```

### 3.2 进件流程（自收 vs 代收）

```
1. 提交进件:  POST /api/onboarding
   → 创建记录，状态=草稿
   → 调用支付中台（模拟），状态=审核中
   → 返回 applyNo

2. 模拟回调:  POST /api/onboarding/{id}/callback?approved=true
   → 通过: 状态=已入驻，返回 channelMctNo
   → 驳回: 状态=驳回，记录原因
```

### 3.3 自收 vs 代收的区别

```
自收（bizType=1）:
  资金流: 用户 → 商户自己的商户号
  进件: 商户需申请自己的支付商户号
  费用: 平台只统计服务费，通道费商户自己查

代收（bizType=2）:
  资金流: 用户 → Qingo LLC → 商户
  进件: 平台统一收款
  费用: 平台可获取完整交易数据，含通道费
```

---

## 四、如何排查问题

### 4.1 看日志

```bash
# 日志级别分三档，从少到多:
ERROR   → 系统异常，必须修复
WARN    → 业务异常，参数错误
DEBUG   → 详细信息，SQL 语句

# 重点看:
# 1. 请求进来了没有（Controller 日志）
# 2. 执行了什么 SQL（MyBatis 日志）
# 3. 异常是什么（Exception 堆栈）
```

### 4.2 常见问题定位

| 现象 | 排查方向 |
|------|---------|
| 请求 404 | 检查 Controller 的 `@RequestMapping` 路径和 HTTP 方法 |
| 请求 400 | 参数校验失败，看响应中的 `msg` 字段 |
| 请求 500 | 看控制台 Exception 堆栈 |
| SQL 报错 | 看日志中的 SQL 语句，复制到数据库直接执行测试 |
| 事务不回滚 | 检查 `@Transactional` 是否加在 public 方法上 |
| 注入为 null | 检查 `@Autowired` 或构造函数注入是否正确 |

### 4.3 调试技巧

```bash
# 1. 直接调 API（用前端页面或 Postman）
curl http://localhost:8080/api/merchants

# 2. 看 SQL（MyBatis StdOutImpl 会在控制台打印 SQL）
# 3. 查数据库（用 Navicat 连接 localhost:3306，root/root）
# 4. 加日志（在关键位置加 log.info/debug）
```

---

## 五、切换到 MySQL 配置

### MySQL 安装（已安装 8.0.28）

```bash
# 安装位置: C:\mysql-8.0.28-winx64
# 软链接: C:\mysql → C:\mysql-8.0.28-winx64
# 服务名: MySQL
# 端口: 3306
# root 密码: root
# 数据库: oversea_learning

# 常用命令:
net start MySQL    # 启动
net stop MySQL     # 停止
mysql -u root -p   # 登录（密码 root）
```

### 配置说明

当前 `application.yml` 已配置为 MySQL，直接 `mvn spring-boot:run` 即可。

如果想用 H2 内存数据库（无需 MySQL），可以把 `application.yml` 中的配置改为 H2 的连接字符串，或者直接切换到 `application-dev.yml` 中的配置。

---

## 六、学习路线

```
1. 看懂四层架构（★ 本次改造的核心）
   → 读 controller/MerchantController.java        （只接参数）
   → 读 biz/impl/BizMerchantServiceImpl.java       （业务逻辑都在这）
   → 读 service/impl/MerchantServiceImpl.java      （纯数据操作）
   → 读 dao/MerchantMapper.java + mapper/MerchantMapper.xml
   → 重点理解: 哪些逻辑放 Biz 层，哪些放数据层

2. 理解 Biz 层的价值（看进件流程，跨了多个数据层）
   → 读 biz/impl/BizOnboardingServiceImpl.java
   → 注意它同时注入了 MerchantService 和 OnboardingService
   → 从前端页面测试进件流程，观察事务和防重复校验

3. 理解自收/代收业务
   → 对照「三、业务场景演练 3.3」
   → 看 Biz 层如何根据业务规则做校验

4. 理解异常处理
   → 读 common/GlobalExceptionHandler.java
   → 读 common/BusinessException.java
   → 看 Biz 层抛异常、Controller 不 catch、全局兜底的配合

5. 理解事务
   → 看 @Transactional 为什么加在 Biz 层而不是数据层
   → 在 BizOnboardingServiceImpl.submit() 中间故意抛异常，验证回滚

6. 实战: 增加一个功能（比如"查询订单列表"）
   → 按四层各写一遍:
     entity/TradeOrder.java（已有）
     dao/TradeOrderMapper.java + mapper/TradeOrderMapper.xml
     service/TradeOrderService.java + impl（数据层）
     biz/BizTradeOrderService.java + impl（业务层）
     controller/TradeOrderController.java
```

---

## 七、Biz 层改造说明（本次新增）

如果你之前看过没有这个层的版本，这里是变化总结：

| 文件 | 变化 |
|------|------|
| `biz/BizMerchantService(Impl)` | 新增，承接原 ServiceImpl 的业务逻辑 |
| `biz/BizOnboardingService(Impl)` | 新增，并增强了校验（商户状态、防重复提交、状态流转） |
| `service/MerchantService(Impl)` | 下沉为纯数据层，方法改为 save/update/getByEmail 等 |
| `service/OnboardingService(Impl)` | 下沉为纯数据层 |
| 两个 Controller | 注入对象从 `XxxService` 改为 `BizXxxService` |

接口路径、请求参数、返回结构完全不变，前端页面无需改动。