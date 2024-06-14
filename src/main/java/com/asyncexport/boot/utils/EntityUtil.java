package com.asyncexport.boot.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.enums.BooleanEnum;
import com.asyncexport.boot.service.BzAsyncExportLogService;
import org.apache.poi.hpsf.Decimal;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EntityUtil {

    private static final Map<String, String> typeMapping;

    //数据库字段映射
    static {
        typeMapping = new HashMap<>();
        typeMapping.put("INTEGER", "Integer");
        typeMapping.put("INT", "int");
        typeMapping.put("tinyint", "int");
        typeMapping.put("BIGINT", "Long");
        typeMapping.put("SMALLINT", "Short");
        typeMapping.put("FLOAT", "Float");
        typeMapping.put("DOUBLE", "Double");
        typeMapping.put("DECIMAL", "java.math.BigDecimal");
        typeMapping.put("VARCHAR", "String");
        typeMapping.put("text", "String");
        typeMapping.put("CHAR", "String");
        typeMapping.put("BOOLEAN", "Boolean");
        typeMapping.put("DATE", "java.util.Date");
        typeMapping.put("DATETIME", "java.util.Date");
        typeMapping.put("TIME", "java.sql.Time");
        typeMapping.put("TIMESTAMP", "java.sql.Timestamp");
        typeMapping.put("BLOB", "byte[]");
    }

    public static void main(String[] args) throws FileNotFoundException {
        //实体类输出路径
        String path = "src/main/java/com/asyncexport/boot/entity/";
        //数据库配置的 yml位置 用于读取数据库配置 spring.datasource.url。。。。。
        String ymlPath = "src/main/resources/application.yml";
        //表名
        String tableName = "tableName";
        createClass(path,tableName,ymlPath);
    }

    /**
     * 生成导出实体类
     * @param path 生成路径 src/main/java/com/asyncexport/boot/entity/
     * @param tableName 要生成的表
     * @param ymlPath 数据库配置路径 src/main/resources/application.yml
     * @throws FileNotFoundException
     */
    private static void createClass(String path,String tableName,String ymlPath) throws FileNotFoundException {
        InputStream input = new FileInputStream(ymlPath);

        Yaml yaml = new Yaml();
        // 读取 YAML 文件到 Map 对象
        Map<String, Object> yamlData = yaml.load(input);

        // 获取数据源参数
        Map<String, Object> dataSourceConfig = (Map<String, Object>) ((Map<String, Object>) yamlData.get("spring")).get("datasource");
        String jdbcUrl = (String) dataSourceConfig.get("url");
        String username = (String) dataSourceConfig.get("username");
        String password = dataSourceConfig.get("password").toString();
        try {
            //
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                DatabaseMetaData metaData = connection.getMetaData();

                ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
                HashMap columnMap = new HashMap();

                StringBuilder stringBuilder = new StringBuilder();

                // 生成实体类
                classTitle(path,tableName,stringBuilder);

                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    String columnType = resultSet.getString("TYPE_NAME");
                    String columnComment = resultSet.getString("REMARKS");

                    if (StrUtil.isBlank(columnComment) || columnMap.containsKey(columnName)){
                        continue;
                    }else {
                        columnMap.put(columnName, columnComment);
                    }

                    generateMyBatisEntityClass(columnName, columnType, columnComment,stringBuilder);
                }
                stringBuilder.append("}");

                fileOut(stringBuilder.toString(), path , captureNameUppercase(hump(tableName))+"ExportDTO" + ".java");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件输出执行方法
     * @param text
     * @param path
     * @param fileName
     */
    private static void fileOut(String text, String path,String fileName) {

        File mkdir = new File(path);

        mkdir.mkdir();

        File file = new File(path+fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 类头部信息
     * @param tableName
     * @param stringBuilder
     */
    private static void classTitle(String path,String tableName, StringBuilder stringBuilder) {

        stringBuilder.append("package ").append(path.replace("/",".").substring(14,path.length()-1)).append(";\n\n")
                .append("import com.alibaba.excel.annotation.ExcelProperty;\n")
                .append("import com.alibaba.excel.annotation.write.style.ColumnWidth;\n")
                .append("import com.alibaba.excel.annotation.write.style.HeadRowHeight;\n")
                .append("import com.baomidou.mybatisplus.annotation.TableField;\n")
                .append("import com.alibaba.excel.enums.BooleanEnum;\n")
                .append("import com.alibaba.excel.annotation.write.style.HeadFontStyle;\n")
                .append("import com.baomidou.mybatisplus.annotation.TableName;\n")
                .append("import lombok.Data;\n")
                .append("import lombok.experimental.Accessors;\n\n\n")
                .append("@Data\n")
//                .append("@Accessors(chain = true)\n")
                .append("@TableName(\"").append(tableName).append("\")\n")
                .append("@HeadFontStyle(bold = BooleanEnum.TRUE, fontHeightInPoints = 10)\n")
                .append("@HeadRowHeight(value = 25)\n")
                .append("public class ").append(captureNameUppercase(hump(tableName))).append("ExportDTO ").append("implements Serializable{\n\n")
                .append("private static final long serialVersionUID = 1L;\n\n");
    }

    /**
     * 类字段信息
     * @param columnName
     * @param columnType
     * @param columnComment
     * @param stringBuilder
     */
    private static void generateMyBatisEntityClass(String columnName, String columnType, String columnComment, StringBuilder stringBuilder) {
        stringBuilder.append("  @ExcelProperty(\"").append(columnComment).append("\")\n")
                .append("  @ColumnWidth(").append(getJavaTypeSize(columnType)).append(")\n")
                .append("  @TableField(value = \"").append(columnName).append("\")\n")
                .append("  private ").append(typeMapping.get(columnType) == null ? "Object" : typeMapping.get(columnType)).append(" ").append(hump(columnName)).append(";\n\n");
    }


    /**
     * 转为驼峰结构
     * @param columnName
     * @return
     */
    private static String hump(String columnName){
        String name = columnName.toLowerCase(Locale.ROOT);

        String[] split = name.split("_");

        StringBuilder stringBuilder = new StringBuilder(split[0]);

        for (int i = 1; i < split.length; i++) {
            stringBuilder.append(captureNameUppercase(split[i]));
        }
       return stringBuilder.toString();
    }

    //首字母大写
    public static String captureNameUppercase(String name) {
        char[] cs = name.toCharArray();
        //已经是大写
        if (cs[0] >= 'A' && cs[0] <= 'Z') {
            return name;
        }
        cs[0] -= 32;
        return String.valueOf(cs);

    }

    /**
     * 表格导出修饰
     * @param dbType
     * @return
     */
    private static int getJavaTypeSize(String dbType) {
        // Your type mapping logic
        switch (dbType.toUpperCase()) {
            case "VARCHAR":
            case "CHAR":
                return 22;
            case "INT":
                return 6;
            case "DATETIME":
            case "DATE":
            case "TIME":
                return 23;
            default:
                return 15;
        }
    }
}
