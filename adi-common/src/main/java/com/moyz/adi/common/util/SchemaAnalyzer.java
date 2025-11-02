package com.moyz.adi.common.util;

import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 存字段信息（字段名+字段类型）
record ColumnInfo(String name, String type) {
}

@Slf4j
public class SchemaAnalyzer {

    // 数据库连接信息（就是之前让你记的那三个！）
    private static final String URL = "jdbc:mysql://localhost:3306/hz_test";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";
    // 每个表只取3条数据当例子，太多了AI看不过来
    private static final int EXAMPLES_LIMIT = 3;


    // 连数据库并初始化表和数据
    public static Connection getInitialConnection() {
        // 1. 连数据库
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            log.info("数据库连接异常", e);
            throw new BaseException(ErrorEnum.B_DB_CONN_ERROR, "数据库连接异常");
        }
        System.out.println("成功连到PostgreSQL啦！");
        return connection;
    }

    // 生成数据库结构描述（给AI看的“说明书”）
    public static String getSchemaDescription(Connection connection, String tableName)   {
        if (connection == null) {
            throw new IllegalArgumentException("数据库没连上，别瞎传！");
        }
        StringBuilder schemaDesc = new StringBuilder();
        // 1. 先获取所有表名
        //List<String> tables = getTables(connection);
        List<String> tables = null;
        try {
            tables = getOneTable(connection, tableName);
        } catch (SQLException e) {
            log.info("数据库连接异常", e);
            throw new BaseException(ErrorEnum.B_DB_CONN_ERROR, "数据库连接异常");
        }
        // 2. 逐个表整理结构和示例数据
        for (String table : tables) {
            schemaDesc.append("表名：").append(table).append("\n");
            schemaDesc.append(getTableDetail(connection, table));
        }
        return schemaDesc.toString();
    }

    // 整理单个表的结构（字段名+类型）和示例数据
    private static String getTableDetail(Connection connection, String table) {
        StringBuilder detail = new StringBuilder();
        // 获取表的所有字段
        List<ColumnInfo> columns = null;
        List<List<String>> sampleRows = null;
        try {
            columns = getColumns(connection, table);
            for (ColumnInfo col : columns) {
                detail.append("  - ").append(col.name()).append("（类型：").append(col.type()).append("）\n");
            }
            // 获取表的示例数据
            sampleRows = getSampleRows(connection, table, columns);
        } catch (SQLException e) {
            log.info("数据库连接异常", e);
            throw new BaseException(ErrorEnum.B_DB_CONN_ERROR, "数据库连接异常");
        }
        // 把示例数据写成表格格式，AI看得更清楚
        detail.append("示例数据：\n");
        detail.append("  | ");
        for (ColumnInfo col : columns) {
            detail.append(col.name()).append(" | ");
        }
        detail.append("\n");
        for (List<String> row : sampleRows) {
            detail.append("  | ");
            for (String value : row) {
                detail.append(value).append(" | ");
            }
            detail.append("\n");
        }
        return detail.toString();
    }

    // 获取数据库里的所有表名
    private static List<String> getTables(Connection connection) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getTables(null, "public", "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    // 获取数据库里的所有表名
    private static List<String> getOneTable(Connection connection, String tableName) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getTables(null, "public", "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tableName.equals(tName)) {
                    tables.add(tName);
                }
            }
        }
        return tables;
    }

    // 获取单个表的所有字段
    private static List<ColumnInfo> getColumns(Connection connection, String table) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getColumns(null, "public", table, "%")) {
            while (rs.next()) {
                columns.add(new ColumnInfo(rs.getString("COLUMN_NAME"),  // 字段名
                        rs.getString("TYPE_NAME")     // 字段类型
                ));
            }
        }
        return columns;
    }

    private static List<List<String>> getSampleRows(Connection connection, String table, List<ColumnInfo> columns) throws SQLException {
        var rows = new ArrayList<List<String>>();
        try (ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + table + " LIMIT " + EXAMPLES_LIMIT)) {
            while (rs.next()) {
                var row = new ArrayList<String>();
                for (ColumnInfo col : columns) {
                    row.add(rs.getString(col.name()));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    public static String getSqlFromCodeBlock(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 去除开头的 ```sql 或 ```任何语言
        String result = input.trim();
        if (result.contains("```sql") && result.contains("```")) {
            // 正则表达式匹配 ```json 和 ``` 之间的内容（包括换行）
            Pattern pattern = Pattern.compile("```sql\\n(.*?)\\n```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                result = matcher.group(1);
            }
        }
        if (result.contains("<sql>") && result.contains("</sql>")) {
            // 正则表达式匹配 ```json 和 ``` 之间的内容（包括换行）
            Pattern pattern = Pattern.compile("<sql>\\n(.*?)\\n</sql>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                result = matcher.group(1);
            }
        }
        return result;
    }

    public static List<String> extractSql2List(String input) {
        List<String> sqlList = new ArrayList<>();

        // 正则表达式匹配<sql>和</sql>标签及其内容
        Pattern pattern = Pattern.compile("<sql>\\s*(.*?)\\s*</sql>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String sql = matcher.group(1).trim();
            if (!sql.isEmpty()) {
                sqlList.add(sql);
            }
        }

        return sqlList;
    }
}