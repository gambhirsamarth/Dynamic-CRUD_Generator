package com.hackathon.deployer.service;

import com.hackathon.deployer.dao.DynamicDao;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DynamicService {

    @Autowired
    DynamicDao dynamicDao;

    public void createTable(String tableName, JSONObject fields) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty.");
        }
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Fields cannot be null or empty.");
        }
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" (");

        int count = 0;
        for (Object key : fields.keySet()) {
            String columnName = (String) key;
            String columnType = fields.getString(columnName);
            sql.append(columnName).append(" ").append(columnType);
            if (count < fields.size() - 1) {
                sql.append(", ");
            }
            count++;
        }

        sql.append(");");
        dynamicDao.executeQuery(sql.toString());
    }


    public void alterTable(String tableName, JSONObject fields, List<Map<String, Object>> tableDescription) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty.");
        }
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Fields cannot be null or empty.");
        }
        if (tableDescription == null || tableDescription.isEmpty()) {
            throw new IllegalArgumentException("Table description cannot be null or empty.");
        }

        List<String> existingColumns = tableDescription.stream().map(column -> column.get("Field").toString().toLowerCase()).toList();

        StringBuilder alterQueryBuilder = new StringBuilder("ALTER TABLE " + tableName);

        boolean first = true;
        for (Object key : fields.keySet()) {
            String columnName = (String) key;
            String columnType = fields.getString(columnName);
            if (!existingColumns.contains(columnName.toLowerCase())) {
                if (!first) {
                    alterQueryBuilder.append(",");
                }
                alterQueryBuilder.append(" ADD COLUMN ").append(columnName).append(" ").append(columnType);
                first = false;
            }
        }
        if (first) {
            System.out.println("No new columns to add to table: " + tableName);
            return;
        }

        String alterQuery = alterQueryBuilder.toString();
        dynamicDao.executeQuery(alterQuery);
    }

    public List<Map<String, Object>> describeTable(String tableName) {
        return dynamicDao.describeTable(tableName);
    }
}
