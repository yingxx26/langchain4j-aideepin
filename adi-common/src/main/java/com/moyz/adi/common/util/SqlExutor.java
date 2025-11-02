package com.moyz.adi.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SqlExutor {

    // 数据库配置
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/hz_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    /**
     * 执行查询并将结果转换为JSON字符串
     */
    public static String executeQueryToJson(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // 加载驱动并建立连接
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);

            // 创建预编译语句防止SQL注入
            preparedStatement = connection.prepareStatement(sql);

            // 设置参数
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            // 执行查询
            resultSet = preparedStatement.executeQuery();

            // 转换结果为JSON
            return convertResultSetToJson(resultSet);

        } catch (Exception e) {
            throw new RuntimeException("数据库查询失败: " + e.getMessage(), e);
        } finally {
            // 关闭资源
            closeResources(resultSet, preparedStatement, connection);
        }
    }

    /**
     * 将ResultSet转换为JSON字符串
     */
    private static String convertResultSetToJson(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            resultList.add(row);
        }

        // 使用FastJSON进行序列化
        return JSON.toJSONString(resultList);
    }

    /**
     * 将ResultSet转换为JSONArray对象
     */
    public static JSONArray executeQueryToJsonArray(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            resultSet = preparedStatement.executeQuery();

            JSONArray jsonArray = new JSONArray();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    jsonObject.put(columnName, value);
                }
                jsonArray.add(jsonObject);
            }

            return jsonArray;

        } catch (Exception e) {
            throw new RuntimeException("数据库查询失败: " + e.getMessage(), e);
        } finally {
            closeResources(resultSet, preparedStatement, connection);
        }
    }

    /**
     * 执行查询并返回单个JSONObject
     */
    public static JSONObject executeQueryToJsonObject(String sql, Object... params) {
        JSONArray jsonArray = executeQueryToJsonArray(sql, params);
        if (jsonArray != null && !jsonArray.isEmpty()) {
            return jsonArray.getJSONObject(0);
        }
        return new JSONObject();
    }

    /**
     * 关闭数据库资源
     */
    private static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("关闭资源时出错: " + e.getMessage());
                }
            }
        }
    }


}