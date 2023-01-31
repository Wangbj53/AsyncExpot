package com.asyncexport.boot.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * @deprecated Wang bin jie
 * 创建异步导出任务
 */
public @interface AsyncExportTask {
    /**
     * 文件名称
     * @return
     */
    String name() default "";

    /**
     * 方法路径
     * @return
     */
    String methodPath() default "";

    /**
     * 二进制文件输出路径
     */
    String outMethodPath() default "";

    /**
     * 0:异步 或者 1:同步
     */
    int syncFlag() default 0;

}
