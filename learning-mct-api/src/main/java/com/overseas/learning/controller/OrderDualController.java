package com.overseas.learning.controller;

import com.overseas.learning.biz.BizOrderDualService;
import com.overseas.learning.common.Result;
import com.overseas.learning.dto.OrderCreateDto;
import com.overseas.learning.entity.TradeOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 订单双写演示接口 —— 演示「下单同时写 MySQL + ES」
 *
 * 【测试流程】
 *   1. 下单:        POST /dual/order?merchantId=1001&amount=500
 *   2. 对比两边:    GET  /dual/compare          （看 MySQL 和 ES 是否一致）
 *   3. 只查 MySQL:  GET  /dual/mysql
 *   4. 只查 ES:     GET  /dual/es?keyword=ORD&status=0
 *   5. 支付:        POST /dual/pay?id=1
 *   6. 手动同步:    POST /dual/sync             （MySQL → ES 全量刷）
 *   7. 删除:        DELETE /dual/order?id=1&esDocId=1
 */
@Slf4j
@RestController
@RequestMapping("/dual")
@RequiredArgsConstructor
public class OrderDualController {

    private final BizOrderDualService bizOrderDualService;

    /**
     * 下单（★ 同时写 MySQL + ES）
     *
     * 【对齐 qingo】Controller 只接收 DTO + @Valid 校验，
     *   不拼实体；DTO → Entity 转换在 Biz 层做。
     *
     * POST /dual/order
     * Body(JSON): { "merchantId":1001, "shopId":1, "amount":500, "receiveType":1, "paymentMethod":"WECHAT" }
     */
    @PostMapping("/order")
    public Result<TradeOrder> createOrder(@Valid @RequestBody OrderCreateDto dto) {
        return Result.success(bizOrderDualService.createOrder(dto));
    }

    /**
     * 对比 MySQL 和 ES 两边的数据（★ 演示数据一致性）
     * GET /dual/compare
     */
    @GetMapping("/compare")
    public Result<Map<String, Object>> compare() {
        return Result.success(bizOrderDualService.compareData());
    }

    /**
     * 只查 MySQL（详情/管理走 MySQL）
     * GET /dual/mysql
     */
    @GetMapping("/mysql")
    public Result<List<TradeOrder>> mysql() {
        return Result.success(bizOrderDualService.listFromMysql());
    }

    /**
     * 只查 ES（搜索走 ES）
     * GET /dual/es
     */
    @GetMapping("/es")
    public Result<Map<String, Object>> es(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status) {
        return Result.success(bizOrderDualService.listFromEs(keyword, status));
    }

    /**
     * 支付订单（★ 同时更新 MySQL + ES 状态）
     * POST /dual/pay?id=1
     */
    @PostMapping("/pay")
    public Result<Void> pay(@RequestParam Long id) {
        bizOrderDualService.payOrder(id);
        return Result.success();
    }

    /**
     * 手动同步 MySQL → ES（★ 对齐 qingo 的 DbEsSyncService 定时同步）
     * POST /dual/sync
     */
    @PostMapping("/sync")
    public Result<Integer> sync() {
        return Result.success(bizOrderDualService.syncMysqlToEs());
    }

    /**
     * 删除订单（★ 同时删 MySQL + ES）
     * DELETE /dual/order?id=1&esDocId=1
     */
    @DeleteMapping("/order")
    public Result<Void> delete(@RequestParam Long id, @RequestParam String esDocId) {
        bizOrderDualService.deleteOrder(id, esDocId);
        return Result.success();
    }
}
