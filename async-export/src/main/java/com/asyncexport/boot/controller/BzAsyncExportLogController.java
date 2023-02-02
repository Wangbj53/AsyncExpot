package com.asyncexport.boot.controller;

import com.asyncexport.boot.entity.BzAsyncExportLog;
import com.asyncexport.boot.entity.PageQuery;
import com.asyncexport.boot.service.BzAsyncExportLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 异步导出表Controller
 *
 * @author wangBj
 */
@RestController
@RequestMapping("/bzAsyncExportLog")
public class BzAsyncExportLogController {
    @Resource
    BzAsyncExportLogService bzAsyncExportLogService;

    /**
     * 测试
     */
    @PostMapping("/export")
    public void export() {
         this.bzAsyncExportLogService.export();
    }

}
