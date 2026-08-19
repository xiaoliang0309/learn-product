package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.entity.OperateLogDoc;
import com.overseas.learning.service.OperateLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志查询接口 —— 演示从 MongoDB 读操作日志
 *
 * GET /operate-log/list            → 查全部
 * GET /operate-log/list?targetType=merchant&targetId=1  → 按对象查
 */
@RestController
@RequestMapping("/operate-log")
@RequiredArgsConstructor
public class OperateLogController {

    private final OperateLogService operateLogService;

    @GetMapping("/list")
    public Result<List<OperateLogDoc>> list(@RequestParam(required = false) String targetType,
                                            @RequestParam(required = false) Long targetId) {
        if (targetType != null && targetId != null) {
            return Result.success(operateLogService.listByTarget(targetType, targetId));
        }
        return Result.success(operateLogService.listAll());
    }
}
