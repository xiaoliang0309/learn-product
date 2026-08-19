package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.entity.DeviceLogDoc;
import com.overseas.learning.service.DeviceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MongoDB 演示接口 —— 演示 MongoDB 的读写
 *
 * 【测试】
 *   写: POST /mongo/log?deviceCode=HG001&action=open_door&content={...}
 *   查: GET  /mongo/log/list?deviceCode=HG001
 *   查单个: GET /mongo/log/{id}（id 用写入时返回的）
 */
@RestController
@RequestMapping("/mongo")
@RequiredArgsConstructor
public class MongoDemoController {

    private final DeviceLogService deviceLogService;
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    /** 写一条设备日志 */
    @PostMapping("/log")
    public Result<DeviceLogDoc> save(@RequestParam String deviceCode,
                                     @RequestParam String action,
                                     @RequestParam(defaultValue = "{}") String content) {
        return Result.success(deviceLogService.saveLog(deviceCode, action, content));
    }

    /** 按 ID 查 */
    @GetMapping("/log/{id}")
    public Result<DeviceLogDoc> getById(@org.springframework.web.bind.annotation.PathVariable String id) {
        return Result.success(deviceLogService.getById(id));
    }

    /** 按设备编码查列表 */
    @GetMapping("/log/list")
    public Result<List<DeviceLogDoc>> list(@RequestParam String deviceCode) {
        return Result.success(deviceLogService.listByDevice(deviceCode));
    }

    /**
     * 演示 MongoDB 灵活 schema：用 Map 写「类里没定义的字段」
     *
     * 这个接口往 doc_device_log 集合写一条文档，但带了几个 DeviceLogDoc 类里
     * 没定义的字段（temperature、extra_info、custom_field）。
     * 你在 Navicat 里能看到：这些没定义的字段也照样存进去了 —— 这就是灵活 schema。
     *
     * POST /mongo/flexible
     */
    @PostMapping("/flexible")
    public Result<Object> writeFlexible() {
        // 用 org.bson.Document（MongoDB 原生文档），可以放任意字段，不受实体类限制
        org.bson.Document doc = new org.bson.Document();
        doc.put("device_code", "HG-FLEX");
        doc.put("action", "flexible_demo");

        // 下面这些字段，DeviceLogDoc 类里都没有定义 —— 但 MongoDB 照样能存
        doc.put("temperature", 5);                                  // 未定义字段1：数字
        // 嵌套对象（Java 8 没有 Map.of，用 HashMap）
        java.util.Map<String, Object> extra = new java.util.HashMap<>();
        extra.put("door", "ok");
        extra.put("camera", 2);
        doc.put("extra_info", extra);                               // 未定义字段2：嵌套对象
        doc.put("custom_field", "我是类里没有的字段");                    // 未定义字段3：字符串

        org.bson.Document saved = mongoTemplate.save(doc, "doc_device_log");
        return Result.success(saved);
    }
}
