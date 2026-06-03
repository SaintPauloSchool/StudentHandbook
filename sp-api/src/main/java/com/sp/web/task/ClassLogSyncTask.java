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
 * 課程日誌數據同步定時任務
 * 每週一到週五午5點40分執行課程日誌數據同步（北京時間）
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
     * 每週一到週五17點50分執行課程日誌數據同步（北京時間）
     */
    //@Scheduled(cron = "0 50 17 ? * MON-FRI", zone = "Asia/Shanghai")
    public void syncClassLogData() {
        // 使用AtomicBoolean確保同一時間只有一個實例在執行
        if (!isExecuting.compareAndSet(false, true)) {
            logger.info("課程日誌數據同步任務已在執行中，跳過本次執行");
            return;
        }

        try {
            logger.info("開始執行課程日誌數據同步任務");

            // 從外部數據庫獲取所有課程日誌數據
            List<ClassLog> classLogs = externalClassLogService.getAllClassLogsFromExternal();

            if (classLogs != null && !classLogs.isEmpty()) {
                logger.info("從外部數據庫獲取到 {} 條課程日誌數據", classLogs.size());

                // 將數據傳輸到目標數據庫
                classLogService.batchUpsertClassLogs(classLogs);

                logger.info("課程日誌數據同步任務完成");
            } else {
                logger.info("外部數據庫中沒有課程日誌數據需要同步");
            }
        } catch (Exception e) {
            logger.error("同步課程日誌數據失敗", e);
        } finally {
            // 確保執行完成後釋放鎖
            isExecuting.set(false);
        }

        logger.info("課程日誌數據同步任務執行完成");
    }
}