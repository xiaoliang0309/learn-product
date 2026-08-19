package com.overseas.learning.biz.impl;

import com.overseas.learning.biz.BizMerchantService;
import com.overseas.learning.common.BusinessException;
import com.overseas.learning.common.RedisKeys;
import com.overseas.learning.common.ResultCode;
import com.overseas.learning.dto.MerchantCreateDto;
import com.overseas.learning.dto.MerchantQueryDto;
import com.overseas.learning.dto.PageResult;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.service.MerchantService;
import com.overseas.learning.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商户「业务层」实现（Biz 层）
 *
 * 【分层职责对照】
 *   ┌─────────────────────────────────────────────────┐
 *   │  Biz 层（本类）                                   │
 *   │  ✔ 业务规则校验（邮箱重复、状态流转是否合法）        │
 *   │  ✔ DTO ↔ Entity 转换                             │
 *   │  ✔ 组合多个数据 Service（跨表/跨模块）              │
 *   │  ✔ 事务控制 @Transactional                       │
 *   │  ✔ 调用外部服务（Feign / 支付中台 / 消息队列）       │
 *   └─────────────────────────────────────────────────┘
 *   ┌─────────────────────────────────────────────────┐
 *   │  数据层（MerchantService / MerchantServiceImpl）   │
 *   │  ✔ 单表 CRUD                                     │
 *   │  ✔ 不写业务判断，不做 DTO 转换                     │
 *   │  ✔ 一个方法对应一个（或少量）Mapper 调用            │
 *   └─────────────────────────────────────────────────┘
 *
 * 【真实项目对照】
 *   qingo 项目里 Biz 层类名示例:
 *     BizGoodsBrandServiceImpl、BizAdventGoodsServiceImpl
 *   它们会注入多个数据层 Service，比如同时注入:
 *     GoodsBrandService + MctShopGoodsService + RedisService
 *   然后在一个方法里编排这些调用，组成完整业务流程。
 *
 * 【关键注解】
 *   @Service               — 注册为 Spring Bean（Biz 层也用 @Service，没有单独的注解）
 *   @RequiredArgsConstructor — Lombok 构造函数注入（final 字段）
 *   @Transactional         — 事务边界放在 Biz 层，因为一个业务方法可能调用多个数据层方法
 *
 * @see BizMerchantService
 * @see MerchantService 数据层接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizMerchantServiceImpl implements BizMerchantService {

    // 注入的是「数据层」Service，而不是直接注入 Mapper
    // 这样 Biz 层只关心「我要什么数据」，不关心「数据怎么查」
    private final MerchantService merchantService;

    // ★ Redis 场景：注入 Redis 操作封装
    private final RedisService redisService;

    // ★ MongoDB 场景：注入操作日志服务（业务数据在 MySQL，操作日志写 MongoDB）
    private final com.overseas.learning.service.OperateLogService operateLogService;

    /** 商户缓存过期时间（秒），10 分钟 */
    private static final long MERCHANT_CACHE_SECONDS = 600;

    @Override
    @Transactional
    public Merchant create(MerchantCreateDto dto) {
        // ========== 1. 业务规则校验 ==========
        // 邮箱唯一是业务约束，不是数据库约束（虽然表里有唯一索引兜底）
        // 放在 Biz 层，可以在抛异常时带上友好的业务提示
        if (dto.getEmail() != null) {
            Merchant exist = merchantService.getByEmail(dto.getEmail());
            if (exist != null) {
                throw new BusinessException(ResultCode.MERCHANT_EMAIL_EXISTS);
            }
        }

        // ========== 2. DTO → Entity 转换（★ 用 Builder，与 qingo 真实项目一致）==========
        // qingo 项目不用 BeanUtils.copyProperties，而是用 Lombok @Builder 逐字段手动构建。
        // 好处：每个字段的来历一目了然，能精确控制（比如这里顺手做默认值和状态），
        //       且字段名写错会在编译期报错，不像 BeanUtils 静默不拷。
        //
        // 对比一下两种写法（功能等价）：
        //   【BeanUtils 写法】
        //     Merchant m = new Merchant();
        //     BeanUtils.copyProperties(dto, m);        // 反射批量拷同名字段
        //     m.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "USD");
        //     m.setStatus(1);
        //   【Builder 写法】（qingo 在用，本项目采用）
        //     见下方代码 —— 默认值、状态都在链式调用里一次性写好
        Merchant merchant = Merchant.builder()
                .fullName(dto.getFullName())
                .shortName(dto.getShortName())
                .bizType(dto.getBizType())
                // 3. 业务默认值直接在构建时设置（比 BeanUtils 少一步手动 set）
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .country(dto.getCountry() != null ? dto.getCountry() : "US")
                .state(dto.getState())
                .city(dto.getCity())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .status(1) // 默认启用
                .build();
        // 注意：id、createdAt、updatedAt 不在 DTO 里，构建时不设置，
        //       由数据库自增/默认值生成（这正是 DTO 和 Entity 分离的意义）

        // ========== 4. 调用数据层写入 ==========
        // 如果这里还需要同时写多张表（比如商户 + 门店 + 银行账户），
        // 就在这一个事务里调用多个数据层 Service，这就是 Biz 层存在的意义
        merchantService.save(merchant);

        // ========== 5. 写操作日志到 MongoDB ==========
        // 业务数据(MySQL)已写入，这里往 MongoDB 记一条「操作日志」。
        // 这就是 qingo 里 MySQL 和 MongoDB 的分工：业务数据 vs 操作流水。
        operateLogService.log("merchant", merchant.getId(), "create",
                "创建商户[" + merchant.getFullName() + "]", "admin");

        log.info("【Biz】创建商户成功: id={}, name={}", merchant.getId(), merchant.getFullName());
        return merchant;
    }

    /**
     * ★ 场景A：查询缓存（Cache Aside 模式，旁路缓存）
     *
     * 这是最经典的 Redis 用法，流程：
     *   1. 先查缓存，命中 → 直接返回，不查数据库（快）
     *   2. 缓存没有 → 查数据库 → 把结果写入缓存 → 返回
     *   3. 下次再查同一 id，就直接走缓存了
     *
     * 好处：数据库压力大减（热点数据都走内存），查询速度快。
     * qingo 里大量详情查询都是这个套路（商品、门店、设备详情等）。
     */
    @Override
    public Merchant getById(Long id) {
        String key = RedisKeys.merchant(id);

        // 1. 先查缓存（用带类型的 get，内部处理好 JSON → Merchant 的转换）
        Merchant cached = redisService.get(key, Merchant.class);
        if (cached != null) {
            log.info("【Biz-缓存】命中缓存，直接返回: key={}", key);
            return cached;
        }

        // 2. 缓存没有 → 查数据库
        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }

        // 3. 查到了 → 写入缓存，下次直接命中
        redisService.set(key, merchant, MERCHANT_CACHE_SECONDS);
        log.info("【Biz-缓存】查库后写入缓存: key={}", key);
        return merchant;
    }

    @Override
    public PageResult<Merchant> pageQuery(MerchantQueryDto query) {
        // 分页查询本身不涉及业务规则，直接透传给数据层
        // 真实项目里这里可能做: 数据权限过滤、查询结果脱敏、关联数据组装
        return merchantService.pageQuery(query);
    }

    @Override
    @Transactional
    public Merchant update(Long id, MerchantCreateDto dto) {
        // 1. 校验目标是否存在（业务规则）
        Merchant merchant = getById(id);

        // 2. 更新字段
        merchant.setFullName(dto.getFullName());
        merchant.setShortName(dto.getShortName());
        merchant.setBizType(dto.getBizType());
        merchant.setCountry(dto.getCountry());
        merchant.setState(dto.getState());
        merchant.setCity(dto.getCity());
        merchant.setAddress(dto.getAddress());
        merchant.setEmail(dto.getEmail());
        merchant.setPhone(dto.getPhone());

        // 3. 调用数据层
        merchantService.update(merchant);

        // 4. ★ 数据变了，删掉旧缓存（下次查询会重新查库并缓存最新值）
        //    这叫「缓存一致性」：写库后必须让旧缓存失效，否则会读到过期数据。
        //    为什么「删除」而不是「更新」缓存？删除更简单安全，避免并发下写覆盖问题。
        redisService.remove(RedisKeys.merchant(id));
        // 写操作日志到 MongoDB
        operateLogService.log("merchant", id, "update", "更新商户[" + merchant.getFullName() + "]", "admin");
        log.info("【Biz】更新商户成功并清缓存: id={}", id);
        return merchant;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 校验存在性
        getById(id);
        merchantService.deleteById(id);
        // 删除后清缓存
        redisService.remove(RedisKeys.merchant(id));
        // 写操作日志到 MongoDB
        operateLogService.log("merchant", id, "delete", "删除商户", "admin");
        log.info("【Biz】删除商户成功并清缓存: id={}", id);
    }
}
