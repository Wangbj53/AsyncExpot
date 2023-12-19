package com.asyncexport.boot.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.alibaba.excel.enums.BooleanEnum;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Data
@Accessors(chain = true)
@TableName("T_CMK_DISPOSE")
@HeadFontStyle(bold = BooleanEnum.TRUE, fontHeightInPoints = 10)
@HeadRowHeight(value = 25)
public class TCmkDisposeExportDTO implements Serializable {

private static final long serialVersionUID = 1L;

  @ExcelProperty("沉默卡码号处置表主键ID")
  @ColumnWidth(15)
  @TableField(value = "DISPOSE_ID")
  private Long disposeId;

  @ExcelProperty("数据上传来源：1：涉诈码号处置上传页面  2：涉诈码号短信下发上传页面  3：数据中心沉默卡SFTP下载")
  @ColumnWidth(6)
  @TableField(value = "DATA_UP_SOURCE")
  private int dataUpSource;

  @ExcelProperty("处置来源（1：工信部下发，2：北京公安邮件，3：公安部邮件，4：公安侦办平台，4001：公安侦办平台QV，4002：电话核查系统侦办平台，5：集团大数据模板一点关停，6：电话核查系统被举报号码关停,7:集团大数据沉默卡单停）")
  @ColumnWidth(6)
  @TableField(value = "DISPOSE_SOURCE")
  private int disposeSource;

  @ExcelProperty("处置类型1:公安涉案双停 ；2大数据保护停机单停； 3:大数据保护停机复机 ；4:公安涉案复机；5:检查系统用户举报号码关停双停；")
  @ColumnWidth(6)
  @TableField(value = "DISPOSE_TYPE")
  private int disposeType;

  @ExcelProperty("停机来源属性-关停推送日期")
  @ColumnWidth(23)
  @TableField(value = "SOURCE_PUSH_TIME")
  private java.util.Date sourcePushTime;

  @ExcelProperty("停机来源属性-数据来源")
  @ColumnWidth(22)
  @TableField(value = "SOURCE_DATA_SOURCE")
  private String sourceDataSource;

  @ExcelProperty("停机来源属性-公安函号")
  @ColumnWidth(22)
  @TableField(value = "SOURCE_LETTER_NO")
  private String sourceLetterNo;

  @ExcelProperty("停机来源属性-公安联系方式")
  @ColumnWidth(22)
  @TableField(value = "SOURCE_CONTACT")
  private String sourceContact;

  @ExcelProperty("函ID/工单ID")
  @ColumnWidth(22)
  @TableField(value = "COMMOAND_ID")
  private String commoandId;

  @ExcelProperty("函下发时间")
  @ColumnWidth(23)
  @TableField(value = "ISSUE_TIME")
  private java.util.Date issueTime;

  @ExcelProperty("待处置码号")
  @ColumnWidth(22)
  @TableField(value = "NUMBER")
  private String number;

  @ExcelProperty("码号类型 1：手机号")
  @ColumnWidth(6)
  @TableField(value = "NUMBER_TYPE")
  private int numberType;

  @ExcelProperty("码号处置去向99：无需处置 1：CRM停复机接口 2：工单下发")
  @ColumnWidth(6)
  @TableField(value = "DIS_DIRECTION")
  private int disDirection;

  @ExcelProperty("短信发送状态提醒短信99：无需下发；0：提醒短信待下发；1：短信发送中；2：提醒短信下发异常； 3： 提醒短信下发失败；9：提醒短信下发成功；")
  @ColumnWidth(22)
  @TableField(value = "SMS_SEND_STATE")
  private String smsSendState;

  @ExcelProperty("提醒短信下发的模板ID")
  @ColumnWidth(6)
  @TableField(value = "SMS_TEMPLATE_ID")
  private int smsTemplateId;

  @ExcelProperty("短信下发到处置下发的时间间隔")
  @ColumnWidth(6)
  @TableField(value = "SMS_DIS_TIME_INTERVAL")
  private int smsDisTimeInterval;

  @ExcelProperty("短信下发后，可进行处置下发的时间")
  @ColumnWidth(23)
  @TableField(value = "SMS_SEND_DIS_TIME")
  private java.util.Date smsSendDisTime;

  @ExcelProperty("码号复开状态：1：已复开 ")
  @ColumnWidth(6)
  @TableField(value = "NUM_REOPEN_STATE")
  private int numReopenState;

  @ExcelProperty("码号状态：0：待处置；1：处置中；2：CRM停复机接口调用失败；3：CRM停复机接口调用返回失败；4：已下发CRM待反馈；5：CRM回调反馈失败；6：72小时内已处置；7：已复开且在可再次关停时间之前：9：处置成功；-1：已入库（待改为待处置）")
  @ColumnWidth(6)
  @TableField(value = "NUMBER_STATE")
  private int numberState;

  @ExcelProperty("CRM停复机接口调用下单的订单流水")
  @ColumnWidth(22)
  @TableField(value = "ORDER_NO")
  private String orderNo;

  @ExcelProperty("省份名称 ")
  @ColumnWidth(22)
  @TableField(value = "PROVINCE_NAME")
  private String provinceName;

  @ExcelProperty("城市名称 ")
  @ColumnWidth(22)
  @TableField(value = "CITY_NAME")
  private String cityName;

  @ExcelProperty("省份城市名称 如：山西晋城")
  @ColumnWidth(22)
  @TableField(value = "PROVINCE_CITY_NAME")
  private String provinceCityName;

  @ExcelProperty("编码为三位数的省份编码，以后可能做省份筛选，运营平台的省份code 例如北京电信101 ")
  @ColumnWidth(22)
  @TableField(value = "PRO_CODE")
  private String proCode;

  @ExcelProperty("CRM停复机接口调用时间")
  @ColumnWidth(23)
  @TableField(value = "CALL_TIME")
  private java.util.Date callTime;

  @ExcelProperty("调用CRM停复机接口返回状态码")
  @ColumnWidth(22)
  @TableField(value = "CALL_STATE")
  private String callState;

  @ExcelProperty("调用CRM停复机接口返回状态码描述")
  @ColumnWidth(22)
  @TableField(value = "CALL_STATE_DESC")
  private String callStateDesc;

  @ExcelProperty("CRM回调时间")
  @ColumnWidth(23)
  @TableField(value = "BACK_TIME")
  private java.util.Date backTime;

  @ExcelProperty("CRM回调接口返回状态码")
  @ColumnWidth(22)
  @TableField(value = "BACK_STATE")
  private String backState;

  @ExcelProperty("CRM回调接口返回状态码描述")
  @ColumnWidth(22)
  @TableField(value = "BACK_STATE_DESC")
  private String backStateDesc;

  @ExcelProperty("CRM回调反馈错误码")
  @ColumnWidth(22)
  @TableField(value = "BACK_ERROR_STATE")
  private String backErrorState;

  @ExcelProperty("CRM回调反馈错误码描述")
  @ColumnWidth(22)
  @TableField(value = "BACK_ERROR_STATE_DESC")
  private String backErrorStateDesc;

  @ExcelProperty("创建时间")
  @ColumnWidth(23)
  @TableField(value = "CREATE_TIME")
  private java.util.Date createTime;

  @ExcelProperty("更新时间")
  @ColumnWidth(23)
  @TableField(value = "UPDATE_TIME")
  private java.util.Date updateTime;

  @ExcelProperty("操作人")
  @ColumnWidth(22)
  @TableField(value = "OPERATOR")
  private String operator;

  @ExcelProperty("备注")
  @ColumnWidth(22)
  @TableField(value = "REMARK")
  private String remark;

  @ExcelProperty("中间号处置表主键ID")
  @ColumnWidth(15)
  @TableField(value = "NUMBER_IVC_ID")
  private Long numberIvcId;

}