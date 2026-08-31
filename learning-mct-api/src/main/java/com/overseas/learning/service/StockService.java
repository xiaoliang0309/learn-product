package com.overseas.learning.service;

import com.baomidou.lock.annotation.Lock4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 库存服务 —— 演示「分布式锁防超卖」（对齐 qingo）
 *
 * 【要解决的问题：超卖】
 *   库存只有 10 件，100 个人同时抢，如果不加锁：
 *     线程A 读到库存=10 → 还没扣 → 线程B 也读到库存=10 → 都扣 → 库存变 -80（超卖）
 *   加锁后：同一时刻只有一个线程能「读库存+扣库存」，不会超卖。
 *
 * 【为什么用分布式锁，不用 synchronized】
 *   synchronized 只能锁住「单个 JVM 进程内」的线程。
 *   微服务是多实例部署的（多个服务进程），synchronized 锁不住别的进程。
 *   分布式锁把锁放在 Redis 里（所有实例共享），哪个实例都能抢同一把锁。
 *
 * 【对齐 qingo】
 *   qingo 用 qinggouyun-component-distributedlock-redisson（lock4j 注解封装）：
 *     @Lock4j(keys = {"#shopId"}, expire = 10000, acquireTimeout = 0)
 *     public void updateShop(...) { ... }
 *   底层就是 Redisson 的 RLock。学习项目直接用 Redisson 原生 tryLock，更易懂。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisService redisService;   // qingo 同款封装，SETNX 版锁要用它的 putIfAbsent

    /**
     * 自身代理（★ Lock4j/事务这类 AOP 注解生效的关键）
     * 在同类里直接 this.deductWithLock4j() 是自调用，不经过 Spring 代理，注解不触发。
     * 注入自己的代理对象 self，通过 self.deductWithLock4j() 调用才会走 AOP 加锁。
     * 用 @Lazy 打破「自己依赖自己」的循环依赖。
     */
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private StockService self;

    private static final String STOCK_KEY = "stock:goods:1";            // 库存放 Redis
    private static final String LOCK_KEY = "lock:stock:goods:1";        // Redisson 锁的 key
    private static final String SETNX_LOCK_KEY = "lock:setnx:goods:1";  // SETNX 锁的 key
    private static final String LOCK4J_KEY = "lock:lock4j:goods:1";     // Lock4j 锁的 key

    /** 压测模式：三种加锁方式对比 */
    public enum LockMode {
        NONE,       // 不加锁（看超卖）
        REDISSON,   // Redisson RLock.tryLock（学习项目原生写法）
        SETNX,      // qingo 同款：RedisService.putIfAbsent 自旋抢锁
        LOCK4J      // qingo 公司规范：@Lock4j 注解（AOP 自动加解锁）
    }

    /**
     * 初始化库存
     */
    public void initStock(int stock) {
        stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(stock));
        log.info("【库存】初始化库存为 {}", stock);
    }

    /**
     * 查当前库存
     */
    public int getStock() {
        String val = stringRedisTemplate.opsForValue().get(STOCK_KEY);
        return val == null ? 0 : Integer.parseInt(val);
    }

    /**
     * 方式1：不加锁扣库存（★ 会超卖，用来对比）
     *
     * 模拟并发下「读-改-写」三步被打断导致超卖。
     */
    public int deductWithoutLock() {
        // ① 读库存
        int stock = getStock();
        if (stock <= 0) {
            return -1; // 没库存
        }
        // ② 模拟业务耗时（调大窗口，让并发更容易撞上「读到同一旧值」，从而复现超卖）
        sleep(50);
        // ③ 扣库存（写回）
        int newStock = stock - 1;
        stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(newStock));
        return newStock;
    }

    /**
     * 方式2：加分布式锁扣库存（★ 不超卖，正确做法）
     *
     * 【Redisson 核心三步】
     *   1. RLock lock = redissonClient.getLock(key)   拿一把锁
     *   2. lock.tryLock(wait, lease, unit)            抢锁（抢不到就等 wait 秒）
     *   3. finally { lock.unlock() }                  用完一定释放（放 finally 防死锁）
     */
    public int deductWithLock() {
        // ① 拿锁（锁的 key 和库存对应，同一件商品共用一把锁）
        RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean locked = false;
        try {
            // ② 抢锁：最多等 3 秒抢锁，抢到后 10 秒自动释放（expire 防死锁）
            //    对齐 qingo 的 @Lock4j(expire = 10000, acquireTimeout = 3000)
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("【锁】抢锁失败（3秒没抢到），稍后重试");
                return -2; // 抢锁失败
            }
            // ↓↓↓ 抢到锁了，这段「读-改-写」同一时刻只有一个线程执行 ↓↓↓
            int stock = getStock();
            if (stock <= 0) {
                return -1;
            }
            sleep(50); // 模拟业务耗时（此时别的线程在锁外等着，进不来）
            int newStock = stock - 1;
            stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(newStock));
            return newStock;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -3;
        } finally {
            // ③ 释放锁（★ 必须放 finally，且只释放自己持有的锁）
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 方式3：qingo 同款 SETNX 锁扣库存（★ 不超卖，正确做法）
     *
     * 【和学习项目 Redisson 版的区别】
     *   qingo 生产环境的 RedisLockUtils 不用 RLock，而是最朴素的 SETNX：
     *     putIfAbsent(key, val, expire)  =  Redis 原生命令「SET key val NX EX」
     *     写入成功 = 抢到锁；写入失败 = 锁被别人占着。
     *   所以 qingo 的「抢锁」本质是：抢不到就报错「操作进行中，请勿重复提交」。
     *   为了能压测出「50 线程都能扣到库存、且不超卖」，这里加了个轻量自旋重试。
     *
     * 【三步】
     *   1. putIfAbsent 抢锁（NX=不存在才写入，EX=10秒自动过期防死锁）
     *   2. 抢到就扣库存
     *   3. finally remove 释放锁
     */
    public int deductWithSetnxLock() {
        // ① 抢锁：自旋重试，最多抢 ~3 秒（每次失败睡 20ms 再试）
        if (!trySetnxLock(SETNX_LOCK_KEY, 10, 3000)) {
            log.warn("【SETNX锁】抢锁失败（3秒没抢到）");
            return -2;
        }
        try {
            // ↓↓↓ 抢到锁了，「读-改-写」同一时刻只有一个线程执行 ↓↓↓
            int stock = getStock();
            if (stock <= 0) {
                return -1;
            }
            sleep(50); // 模拟业务耗时
            int newStock = stock - 1;
            stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(newStock));
            return newStock;
        } finally {
            // ② 释放锁（删 key）。注意：这里对齐 qingo 的 RedisLockUtils，直接 remove。
            //    更严谨应校验 value 是不是自己写入的（防误删别人的锁），压测场景从简。
            redisService.remove(SETNX_LOCK_KEY);
        }
    }

    /**
     * SETNX 抢锁（自旋重试版）
     *
     * @param key        锁的 key
     * @param expireSec  锁自动过期秒数（防死锁）
     * @param timeoutMs  最多抢多久（毫秒），超时返回 false
     */
    private boolean trySetnxLock(String key, long expireSec, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        String val = String.valueOf(System.currentTimeMillis()); // value 随便存个标记
        while (System.currentTimeMillis() < deadline) {
            // putIfAbsent = 「SET key val NX EX」，抢到返回 true
            if (Boolean.TRUE.equals(redisService.putIfAbsent(key, val, expireSec))) {
                return true;
            }
            sleep(20); // 没抢到，睡一会儿再抢（自旋）
        }
        return false;
    }

    /**
     * 方式4：Lock4j 注解版扣库存（★ qingo 公司规范，不超卖，正确做法）
     *
     * 【核心区别：一个注解搞定加锁】
     *   不需要手写「拿锁/抢锁/释放锁」，Lock4j 用 AOP 在方法外面自动包一层：
     *     进方法前 → 按 keys 抢锁；出方法后（含异常）→ 自动释放锁。
     *   这就是 qingo 注释里说的「公司规定分布式锁使用 @Lock4j」。
     *
     * 【注意】
     *   - 必须走 Spring 代理调用才生效：不能 this.deductWithLock4j() 内部自调用，
     *     得注入自身代理（self）来调，否则注解不触发（AOP 失效）。
     *   - executor = RedissonLockExecutor.class 指定底层用 Redisson 实现。
     */
    @Lock4j(keys = {"'" + LOCK4J_KEY + "'"},     // 锁的 key（SpEL 字符串字面量，要加单引号）
            executor = com.baomidou.lock.executor.RedissonLockExecutor.class,
            expire = 10000,        // 锁 10 秒自动释放（防死锁）
            acquireTimeout = 3000) // 最多抢 3 秒，抢不到抛 LockFailureException
    public int deductWithLock4j() {
        // ↓↓↓ 进这个方法时 Lock4j 已经帮你抢到锁了 ↓↓↓
        int stock = getStock();
        if (stock <= 0) {
            return -1;
        }
        sleep(50); // 模拟业务耗时
        int newStock = stock - 1;
        stringRedisTemplate.opsForValue().set(STOCK_KEY, String.valueOf(newStock));
        return newStock;
        // 方法返回（或抛异常）后，Lock4j 自动 unlock，不用手写 finally
    }

    /**
     * 按模式扣库存（压测统一入口）
     */
    public int deductByMode(LockMode mode) {
        switch (mode) {
            case REDISSON:
                return deductWithLock();
            case SETNX:
                return deductWithSetnxLock();
            case LOCK4J:
                // ★ 走自身代理调用，@Lock4j 才会生效
                return self.deductWithLock4j();
            case NONE:
            default:
                return deductWithoutLock();
        }
    }

    /**
     * 并发压测：用线程池模拟 concurrency 个人同时抢库存
     *
     * @param mode        加锁方式（NONE/REDISSON/SETNX/LOCK4J）
     * @param concurrency 并发线程数
     * @return 压测结果（最终库存、成功扣减次数、库存为负说明超卖）
     */
    public StockTestResult concurrentTest(LockMode mode, int initStock, int concurrency) throws InterruptedException {
        initStock(initStock);
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        // ★ 发令枪：让所有线程先就位，听到发令后「同一瞬间」一起抢，并发才真实
        CountDownLatch ready = new CountDownLatch(concurrency);  // 各线程就绪
        CountDownLatch startGun = new CountDownLatch(1);         // 发令枪（只放一次）
        CountDownLatch done = new CountDownLatch(concurrency);   // 各线程完成
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < concurrency; i++) {
            pool.submit(() -> {
                ready.countDown();   // 我就绪了
                try {
                    startGun.await();   // ★ 所有线程卡在这，等发令枪，确保同时起跑
                    int remain = deductByMode(mode);
                    if (remain >= 0) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();               // 等所有线程就绪
        long startMs = System.currentTimeMillis();
        startGun.countDown();        // ★ 发令！所有线程同时冲
        done.await(60, TimeUnit.SECONDS);
        pool.shutdown();
        long cost = System.currentTimeMillis() - startMs;

        int finalStock = getStock();
        boolean oversold = finalStock < 0;
        log.info("【压测】{} 并发 {}, 最终库存 {}, 成功扣减 {}, {}",
                mode, concurrency, finalStock, successCount.get(),
                oversold ? "❌超卖了!" : "✅正常");

        return new StockTestResult(mode.name(), initStock, concurrency, finalStock, successCount.get(), oversold, cost);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 压测结果 DTO
     */
    public static class StockTestResult {
        public String mode;   // NONE / REDISSON / SETNX / LOCK4J
        public int initStock;
        public int concurrency;
        public int finalStock;
        public int successCount;
        public boolean oversold;
        public long costMs;

        public StockTestResult(String mode, int initStock, int concurrency, int finalStock,
                               int successCount, boolean oversold, long costMs) {
            this.mode = mode;
            this.initStock = initStock;
            this.concurrency = concurrency;
            this.finalStock = finalStock;
            this.successCount = successCount;
            this.oversold = oversold;
            this.costMs = costMs;
        }
    }
}
