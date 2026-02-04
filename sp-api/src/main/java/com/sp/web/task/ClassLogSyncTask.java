package com.sp.web.task;

import com.sp.system.entity.ClassLog;
import com.sp.system.service.IClassLogService;
import com.sp.web.service.ExternalClassLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 课程日志数据同步定时任务
 * 每週一到週五午5点40分执行课程日志数据同步（北京时间）
 */
@Component
public class ClassLogSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(ClassLogSyncTask.class);

    @Autowired
    private ExternalClassLogService externalClassLogService;
    
    @Autowired
    private IClassLogService classLogService;

    private static final AtomicBoolean isExecuting = new AtomicBoolean(false);
    
    /**
     * 每週一到週五午5点40分执行课程日志数据同步（北京时间）
     */
    @Scheduled(cron = "0 40 17 ? * MON-FRI", zone = "Asia/Shanghai")
    public void syncClassLogData() {
        // 使用AtomicBoolean确保同一时间只有一个实例在执行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("课程日志数据同步任务已在执行中，跳过本次执行");
            return;
        }
        
        try {
            logger.info("开始执行课程日志数据同步任务");

            // 从外部数据库获取所有课程日志数据
            List<ClassLog> classLogs = externalClassLogService.getAllClassLogsFromExternal();
            
            if (classLogs != null && !classLogs.isEmpty()) {
                logger.info("从外部数据库获取到 {} 条课程日志数据", classLogs.size());

                // 将数据传输到目标数据库
                classLogService.batchUpsertClassLogs(classLogs);

                logger.info("课程日志数据同步任务完成");
            } else {
                logger.info("外部数据库中没有课程日志数据需要同步");
            }
        } catch (Exception e) {
            logger.error("同步课程日志数据失败", e);
        } finally {
            // 确保执行完成后释放锁
            isExecuting.set(false);
        }

        logger.info("课程日志数据同步任务执行完成");
    }
}