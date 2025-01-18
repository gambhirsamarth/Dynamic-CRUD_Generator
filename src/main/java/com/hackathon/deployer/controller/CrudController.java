package com.hackathon.deployer.controller;

import com.hackathon.deployer.service.CrudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/crud")
public class CrudController {

    private final CrudService crudService;

    public CrudController(CrudService crudService) {
        this.crudService = crudService;
    }

    @PostMapping("/insert")
    public ResponseEntity<String> insertRecord(@RequestParam String tableName, @RequestBody Map<String, Object> data) {
        try {
            int rows = crudService .insertRecord(tableName, data);
            return ResponseEntity.ok(rows + " row(s) inserted.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error inserting record: " + e.getMessage());
        }
    }

    @GetMapping("/fetch/{tableName}/{id}")
    public ResponseEntity<?> fetchRecord(@PathVariable String tableName, @PathVariable Long id) {
        try {
            Map<String, Object> record = crudService.fetchRecord(tableName, id);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Record not found: " + e.getMessage());
        }
    }

    @PutMapping("/update/{tableName}/{id}")
    public ResponseEntity<String> updateRecord(
            @PathVariable String tableName,
            @PathVariable Long id,
            @RequestBody Map<String, Object> updatedData) {
        try {
            int rows = crudService.updateRecord(tableName, id, updatedData);
            if (rows > 0) {
                return ResponseEntity.ok("Record updated successfully.");
            } else {
                return ResponseEntity.status(404).body("Record not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating record: " + e.getMessage());
        }
    }
}
