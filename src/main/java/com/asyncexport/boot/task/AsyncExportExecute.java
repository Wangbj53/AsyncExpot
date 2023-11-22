package com.asyncexport.boot.task;

import com.asyncexport.boot.service.BzAsyncExportLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Wangbj
 * @date 2023年11月13日 15:25
 */
@Component
public class AsyncExportExecute {

    @Resource
    private BzAsyncExportLogService bzAsyncExportLogService;

    @Scheduled(cron = "0/1 * * * * ?")
    private void task(){
        bzAsyncExportLogService.export();
    }
}
