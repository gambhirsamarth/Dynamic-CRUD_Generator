package com.hackathon.deployer.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DynamicDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void executeQuery(String query) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty.");
        }
        try {
            jdbcTemplate.execute(query);
        } catch (Exception e) {
            throw new RuntimeException("Error creating table: " + e.getMessage(), e);
        }
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
}
