package com.asyncexport.boot.controller;


import com.asyncexport.boot.base.BaseController;
import com.asyncexport.boot.config.AsyncExportTask;
import com.asyncexport.boot.entity.BzAsyncExportLog;
import com.asyncexport.boot.entity.PageQuery;
import com.asyncexport.boot.service.BzAsyncExportLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Wangbj
 * @date 2023年11月16日 09:47
 */
@RestController
@RequestMapping("bzAsyncExport")
public class BzAsyncExportLogController  extends BaseController<BzAsyncExportLogService> {



    @PostMapping("/export")
    public void export() {
        this.baseService.export();
    }



    @PostMapping("/exportTest")
    @AsyncExportTask(name = "测试大数据量",methodPath = "BzAsyncExportLogService.getPage")
    public void export(@RequestBody PageQuery<BzAsyncExportLog> pageQuery, HttpServletResponse response){

    }


}
