package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分布式锁演示接口 —— 演示「Redisson 分布式锁防超卖」
 *
 * 【测试流程】
 *   1. 初始化库存:    POST /lock/init?stock=10
 *   2. 不加锁压测:    POST /lock/test?useLock=false&stock=10&concurrency=50  → 看超卖（库存变负）
 *   3. 加锁压测:      POST /lock/test?useLock=true&stock=10&concurrency=50   → 不超卖（库存=0）
 *   4. 查当前库存:    GET  /lock/stock
 */
@Slf4j
@RestController
@RequestMapping("/lock")
@RequiredArgsConstructor
public class LockDemoController {

    private final StockService stockService;

    /**
     * 初始化库存
     * POST /lock/init?stock=10
     */
    @PostMapping("/init")
    public Result<Integer> init(@RequestParam(defaultValue = "10") int stock) {
        stockService.initStock(stock);
        return Result.success(stockService.getStock());
    }

    /**
     * 查当前库存
     * GET /lock/stock
     */
    @GetMapping("/stock")
    public Result<Integer> stock() {
        return Result.success(stockService.getStock());
    }

    /**
     * 并发压测（四种模式对比：不加锁 / Redisson / SETNX / Lock4j）
     * POST /lock/test?mode=NONE&stock=10&concurrency=50
     * POST /lock/test?mode=REDISSON&stock=10&concurrency=50
     * POST /lock/test?mode=SETNX&stock=10&concurrency=50
     * POST /lock/test?mode=LOCK4J&stock=10&concurrency=50
     *
     * @param mode        加锁方式：NONE(看超卖) / REDISSON / SETNX(qingo同款) / LOCK4J(qingo规范)
     * @param stock       初始库存
     * @param concurrency 并发线程数（模拟多少人同时抢）
     */
    @PostMapping("/test")
    public Result<StockService.StockTestResult> test(@RequestParam(defaultValue = "NONE") String mode,
                                                     @RequestParam(defaultValue = "10") int stock,
                                                     @RequestParam(defaultValue = "50") int concurrency) {
        try {
            StockService.LockMode lockMode = StockService.LockMode.valueOf(mode.toUpperCase());
            return Result.success(stockService.concurrentTest(lockMode, stock, concurrency));
        } catch (IllegalArgumentException e) {
            return Result.error(400, "mode 只能是 NONE / REDISSON / SETNX / LOCK4J");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.error(500, "压测被中断");
        }
    }
}
