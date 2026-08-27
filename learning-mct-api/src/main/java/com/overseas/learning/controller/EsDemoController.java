package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.dto.DocEsOrderSearchRequest;
import com.overseas.learning.entity.DocEsOrder;
import com.overseas.learning.service.DocEsOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ES 演示接口 —— 演示 Elasticsearch 的读写搜索
 *
 * 【对齐 qingo】
 *   qingo 的 ES 不直接暴露 Controller，而是经 Controller → Biz Service → DocEsXxxService。
 *   学习项目简化：直接暴露 REST 接口，方便前端页面调用和测试。
 *
 * 【测试】
 *   写:   POST /es/order
 *   查:   GET  /es/order/{id}
 *   搜:   GET  /es/order/search?keyword=可乐&status=1&page=1&size=10
 *   统计: GET  /es/order/stats?mctId=1001
 *   造数据: POST /es/order/mock?count=20
 *   删:   DELETE /es/order/{id}
 *   清空: DELETE /es/order/all
 */
@Slf4j
@RestController
@RequestMapping("/es")
@RequiredArgsConstructor
public class EsDemoController {

    private final DocEsOrderService docEsOrderService;

    /**
     * 写入一条订单到 ES
     * POST /es/order
     */
    @PostMapping("/order")
    public Result<DocEsOrder> save(@RequestParam(defaultValue = "1") Integer type,
                                    @RequestParam Integer status,
                                    @RequestParam Long mctId,
                                    @RequestParam String mctName,
                                    @RequestParam String shopCode,
                                    @RequestParam String cabinetCode,
                                    @RequestParam(defaultValue = "Test Goods") String goodsNamesText,
                                    @RequestParam(defaultValue = "1") Integer payPlat,
                                    @RequestParam(defaultValue = "100") Long amount) {
        DocEsOrder doc = DocEsOrder.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .type(type)
                .status(status)
                .mctId(mctId)
                .mctName(mctName)
                .shopCode(shopCode)
                .cabinetCode(cabinetCode)
                .goodsNamesText(goodsNamesText)
                .payPlat(payPlat)
                .amount(amount)
                .createTime(new Date())
                .payTime(status >= 1 ? new Date() : null)
                .build();
        docEsOrderService.save(doc);
        return Result.success(doc);
    }

    /**
     * 按 ID 查询
     * GET /es/order/{id}
     */
    @GetMapping("/order/{id}")
    public Result<DocEsOrder> getById(@PathVariable String id) {
        return Result.success(docEsOrderService.getById(id));
    }

    /**
     * 删除
     * DELETE /es/order/{id}
     */
    @DeleteMapping("/order/{id}")
    public Result<Void> delete(@PathVariable String id) {
        docEsOrderService.delete(id);
        return Result.success();
    }

    /**
     * 搜索订单（分页 + 条件）
     * GET /es/order/search
     *
     * @param keyword  商品名称模糊搜索
     * @param status   订单状态
     * @param mctId    商户ID
     * @param shopCode 店铺编码
     * @param page     页码（从 1 开始）
     * @param size     每页条数
     */
    @GetMapping("/order/search")
    public Result<Map<String, Object>> search(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Long mctId,
                                               @RequestParam(required = false) String shopCode,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        // 对齐 qingo：Controller 把参数装进 request 对象，Service 用对象传条件
        DocEsOrderSearchRequest request = DocEsOrderSearchRequest.builder()
                .keyword(keyword)
                .status(status)
                .mctId(mctId)
                .shopCode(shopCode)
                .build();
        return Result.success(docEsOrderService.search(request, page, size));
    }

    /**
     * 按状态统计订单数（聚合查询）
     * GET /es/order/stats
     */
    @GetMapping("/order/stats")
    public Result<Map<String, Long>> stats(@RequestParam(required = false) Long mctId) {
        return Result.success(docEsOrderService.countByStatus(mctId));
    }

    /**
     * 批量造测试数据（学习用）
     * POST /es/order/mock?count=20
     *
     * 造 count 条随机订单数据，方便演示搜索和统计
     */
    @PostMapping("/order/mock")
    public Result<Integer> mockData(@RequestParam(defaultValue = "20") int count) {
        String[] goodsNames = {"Coca-Cola", "Pepsi", "Sprite", "Water", "Chips", "Chocolate", "Coffee", "Juice", "Gum", "Cookie"};
        String[] mctNames = {"Test Merchant A", "Test Merchant B", "Test Merchant C"};
        long[] mctIds = {1001L, 1002L, 1003L};
        String[] shopCodes = {"HG001", "HG002", "HG003"};
        String[] cabinetCodes = {"HG884120", "HG884121", "HG884122"};

        List<DocEsOrder> docs = IntStream.range(0, count).mapToObj(i -> {
            int idx = ThreadLocalRandom.current().nextInt(goodsNames.length);
            int mctIdx = ThreadLocalRandom.current().nextInt(mctNames.length);
            int status = ThreadLocalRandom.current().nextInt(3); // 0/1/2
            int payPlat = ThreadLocalRandom.current().nextInt(2) + 1; // 1/2
            long amount = (ThreadLocalRandom.current().nextInt(100, 1000)) * 100L;
            Date now = new Date();
            // 随机时间（最近7天内）
            long offset = ThreadLocalRandom.current().nextLong(7 * 24 * 3600L) * 1000L;
            Date createTime = new Date(now.getTime() - offset);

            return DocEsOrder.builder()
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .type(1)
                    .status(status)
                    .mctId(mctIds[mctIdx])
                    .mctName(mctNames[mctIdx])
                    .shopCode(shopCodes[mctIdx])
                    .cabinetCode(cabinetCodes[mctIdx])
                    .goodsNamesText(goodsNames[idx])
                    .payPlat(payPlat)
                    .amount(amount)
                    .createTime(createTime)
                    .payTime(status >= 1 ? createTime : null)
                    .build();
        }).collect(Collectors.toList());

        docEsOrderService.batchSave(docs);
        log.info("【ES】造测试数据: count={}", count);
        return Result.success(count);
    }

    /**
     * 清空索引（学习用：重置数据）
     * DELETE /es/order/all
     */
    @DeleteMapping("/order/all")
    public Result<Void> deleteAll() {
        docEsOrderService.deleteAll();
        return Result.success();
    }
}
