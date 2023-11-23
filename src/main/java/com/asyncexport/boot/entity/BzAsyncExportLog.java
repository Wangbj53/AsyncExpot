package com.asyncexport.boot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 异步导出表实体
 * @Author WangBj
 */
@Data
@TableName("bz_async_export_log")
@Accessors(chain = true)
public class BzAsyncExportLog implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 文件名称
	 */
	private String name;

	/**
	 * 下载文件码
	 */
	private String operationCode;

	/**
	 * 方法路径：类名.方法名
	 */
	private String methodPath;

	/**
	 * 二进制文件输出路径
	 */
	public String OutMethodPath;

	/**
	 * 参数集合
	 */
	private String params;

	/**
	 * 0：导入中;1: 导入成功;2:导入失败;
	 */
	private Integer state;
	/**
	 * 导出操作次数 失败重试三次以后不再操作
	 */
	private Integer exportCount;

	//增加一个同步异步标识
	/**
	 * 0:异步 1：同步
	 */
	@TableField(exist = false)
	private Integer syncFlag = 0;
	/**
	 * 删除标记
	 */
	private int isDelete;

}
