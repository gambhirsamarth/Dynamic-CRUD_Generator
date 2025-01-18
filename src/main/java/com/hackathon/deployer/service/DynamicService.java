package com.hackathon.deployer.service;

import net.sf.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DynamicService {

    private final JdbcTemplate jdbcTemplate;

    public DynamicService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> describeTable(String tableName) {
        try {
            String query = "DESC " + tableName;
            return jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createTable(String tableName, JSONObject fields) {
        // Validate inputs
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty.");
        }
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Fields cannot be null or empty.");
        }

        // Construct the CREATE TABLE SQL query
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" (");

        // Iterate over the field names and types to build the column definitions
        int count = 0;
        for (Object key : fields.keySet()) {
            String columnName = (String) key;
            String columnType = fields.getString(columnName);

            // Append the column definition
            sql.append(columnName).append(" ").append(columnType);

            // Add a comma if it's not the last field
            if (count < fields.size() - 1) {
                sql.append(", ");
            }
            count++;
        }

        sql.append(");"); // Close the SQL query

        // Execute the query using JDBC template
        try {
            jdbcTemplate.execute(sql.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error creating table: " + e.getMessage(), e);
        }
    }
}
