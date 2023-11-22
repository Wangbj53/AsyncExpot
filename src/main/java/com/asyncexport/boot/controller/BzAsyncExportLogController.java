package com.asyncexport.boot.controller;


import com.asyncexport.boot.config.AsyncExportTask;
import com.asyncexport.boot.entity.TCmkDisposeExportDTO;
import com.asyncexport.boot.service.BzAsyncExportLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Wangbj
 * @date 2023年11月16日 09:47
 */
@RestController
@RequestMapping("bzAsyncExport")
public class BzAsyncExportLogController  extends BzAsyncExportLogService{


    @Resource
    private BzAsyncExportLogService baseService;


    @PostMapping("/export")
    public void export() {
        this.baseService.export();
    }


    @PostMapping("/page")
    @AsyncExportTask(name = "测试大数据量",methodPath = "BzAsyncExportLogService.getPage")
    public Page<TCmkDisposeExportDTO> page() {
        return this.baseService.getPage();
    }



}
