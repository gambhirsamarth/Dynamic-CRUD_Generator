package com.hackathon.deployer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CrudService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int insertRecord(String tableName, Map<String, Object> data) {
        // Validate inputs
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty.");
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty.");
        }

        // Build SQL dynamically
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder("VALUES (");

        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sql.append(entry.getKey()).append(", ");
            values.append("?, ");
            params.add(entry.getValue());
        }

        // Remove trailing commas and close the parentheses
        sql.setLength(sql.length() - 2);
        values.setLength(values.length() - 2);
        sql.append(") ").append(values).append(")");

        // Execute the query
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    public Map<String, Object> fetchRecord(String tableName, Long id) {
        // Validate inputs
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty.");
        }
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }

        // Build the SQL query
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";

        // Execute the query and return the result
        return jdbcTemplate.queryForMap(sql, id);
    }

    public int updateRecord(String tableName, Long id, Map<String, Object> updatedData) {
        // Validate inputs
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty.");
        }
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }
        if (updatedData == null || updatedData.isEmpty()) {
            throw new IllegalArgumentException("Updated data cannot be null or empty.");
        }

        // Build the SQL query dynamically
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updatedData.entrySet()) {
            sql.append(entry.getKey()).append(" = ?, ");
            params.add(entry.getValue());
        }

        // Remove trailing comma and add WHERE clause
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        params.add(id);

        // Execute the update query
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }
}
