package com.overseas.learning.job;

import com.overseas.learning.dao.MerchantMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * xxl-job 演示任务 —— 对齐 qingo 的 @XxlJob 写法
 *
 * 【和 @Scheduled 的区别】
 *   @Scheduled：时间写在代码里，项目启动就固定跑，改时间要改代码重启
 *   @XxlJob：代码只写「干什么活」，「什么时候跑、手动触发一次、看日志」
 *            都在调度中心网页上操作，不用改代码、不用重启
 *
 * 【怎么用】
 *   1. 写一个方法，加 @XxlJob("任务名")
 *   2. 到调度中心网页：新建任务 → 填 JobHandler=任务名 → 设执行时间
 *   3. 网页上点「执行一次」或按 cron 自动触发
 *
 * 【@XxlJob("...") 里的字符串】
 *   就是任务的唯一标识，调度中心建任务时 JobHandler 要填一模一样的字符串。
 *   qingo 例子：@XxlJob("orderRecognitPullJobHandler")
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoXxlJob {

    private final MerchantMapper merchantMapper;

    /**
     * 演示任务：统计商户总数
     *
     * JobHandler 名 = "merchantCountJob"
     * 在调度中心建任务时填这个名字，就能触发这个方法。
     *
     * XxlJobHelper.log()：日志会显示在调度中心网页的「调度日志」里，方便排查。
     */
    @XxlJob("merchantCountJob")
    public void merchantCountJob() {
        long total = merchantMapper.count(new com.overseas.learning.entity.Merchant());

        // 既打到项目控制台，也打到调度中心网页的日志里
        log.info("【xxl-job】统计商户总数: total={}", total);
        XxlJobHelper.log("xxl-job 执行成功，商户总数 total={}", total);
    }
}
