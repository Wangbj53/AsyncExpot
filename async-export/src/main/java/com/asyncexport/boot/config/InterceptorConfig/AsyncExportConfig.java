package com.asyncexport.boot.config.InterceptorConfig;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.asyncexport.boot.config.AsyncExportTask;
import com.asyncexport.boot.entity.BzAsyncExportLog;
import com.asyncexport.boot.entity.PageQuery;
import com.asyncexport.boot.service.BzAsyncExportLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 异步注解实现
 * @Author WangBj
 */

@Aspect
@Order
@Component
public class AsyncExportConfig {

    @Autowired
    BzAsyncExportLogService bzAsyncExportLogService;

    @Before("@annotation(com.asyncexport.boot.config.AsyncExportTask)")
    public void beforeTest(JoinPoint joinPoint) {
        //执行
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        AsyncExportTask apiLog = null;
        if (method != null) {
            //拿到自定义注解参数
            apiLog = method.getAnnotation(AsyncExportTask.class);
        } else {
            throw new RuntimeException("AsyncExportTask: 未找到该方法");
        }
        //4. 获取方法的参数 一一对应
        Object[] args = joinPoint.getArgs();
        boolean flag = false;
        //同步导出 异步导出 处理方式不同
        if (apiLog.syncFlag() == 1) {
            BzAsyncExportLog bzAsyncExportLog = new BzAsyncExportLog();
            HttpServletResponse response = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof PageQuery) {
                    PageQuery pageQuery = (PageQuery) args[0];
                    //同步情况
                    bzAsyncExportLog = new BzAsyncExportLog();
                    if (ObjectUtil.isNotNull(pageQuery.getParam())) {
                        bzAsyncExportLog.setParams(JSONObject.toJSONString(pageQuery.getParam()));
                    }
                    bzAsyncExportLog.setName(apiLog.name());
                    bzAsyncExportLog.setMethodPath(apiLog.methodPath());
                    bzAsyncExportLog.setSyncFlag(apiLog.syncFlag());
                    bzAsyncExportLog.setOutMethodPath(apiLog.outMethodPath());
                } else if (args[i] instanceof HttpServletResponse) {
                    response = (HttpServletResponse) args[i];
                }
            }
            try {
                if (ObjectUtil.isNotNull(bzAsyncExportLog) && ObjectUtil.isNotNull(response)){
                    bzAsyncExportLogService.openMain(bzAsyncExportLog, response);
                    flag = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("导出异常！");
            }
        } else {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof PageQuery) {
                    PageQuery pageQuery = (PageQuery) args[0];
                    BzAsyncExportLog bzAsyncExportLog = bzAsyncExportLogService.insert(pageQuery, apiLog.name(), apiLog.methodPath());
                    flag = true;
                    break;
                }
            }
        }
        if (!flag) {
            throw new RuntimeException("当前方法不适用于该注解！类型应为：PageQuery");
        }

    }
}

