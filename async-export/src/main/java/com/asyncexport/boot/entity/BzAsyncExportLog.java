package com.asyncexport.boot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 异步导出表实体
 * @Author WangBj
 */
@Data
@TableName("bz_async_export_log")
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
	private Boolean isDelete;

	/**
	 * 创建人ID
	 */
	@TableField(fill = FieldFill.INSERT)
	private String createUserId;

	/**
	 * 创建人姓名
	 */
	@TableField(fill = FieldFill.INSERT)
	private String createUserName;

	/**
	 * 创建时间
	 */
	@TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
	private Date createTime;

	/**
	 * 修改人ID
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateUserId;

	/**
	 * 修改人姓名
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateUserName;

	/**
	 * 修改时间
	 */
	@TableField(insertStrategy = FieldStrategy.NEVER,updateStrategy = FieldStrategy.NEVER)
	private Date updateTime;


	public static final String COL_ID = "id";

	public static final String COL_OPERATION_CODE = "operation_code";

	public static final String COL_METHOD_PATH = "method_path";

	public static final String COL_PARAMS = "params";

	public static final String COL_STATE = "state";

	public static final String COL_IS_DELETE = "is_delete";

	public static final String COL_CREATE_USER_ID = "create_user_id";

	public static final String COL_CREATE_USER_NAME = "create_user_name";

	public static final String COL_CREATE_TIME = "create_time";

	public static final String COL_UPDATE_USER_ID = "update_user_id";

	public static final String COL_UPDATE_USER_NAME = "update_user_name";

	public static final String COL_UPDATE_TIME = "update_time";

}
