package controller;

import service.AdminService;
import model.SystemStats;
import util.JsonUtil;
import java.util.Map;

public class AdminController {
    private final AdminService adminService;
    private String currentAdminSession = null;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    public String handleLogin(String body) {
        Map<String, Object> data = JsonUtil.parseJson(body);
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        
        var adminOpt = adminService.login(username, password);
        if (adminOpt.isPresent()) {
            currentAdminSession = username;
            return String.format("{\"status\":\"success\",\"message\":\"Welcome %s\",\"role\":\"%s\"}", 
                               username, adminOpt.get().getRole());
        }
        return "{\"status\":\"error\",\"message\":\"Invalid credentials\"}";
    }
    
    public String handleLogout() {
        currentAdminSession = null;
        return "{\"status\":\"success\",\"message\":\"Logged out successfully\"}";
    }
    
    public String handleGetStats() {
        if (currentAdminSession == null) {
            return "{\"error\":\"Unauthorized - Please login first\"}";
        }
        SystemStats stats = adminService.getSystemStats();
        return convertStatsToJson(stats);
    }
    
    public String handleGetResourceSummary() {
        if (currentAdminSession == null) {
            return "{\"error\":\"Unauthorized - Please login first\"}";
        }
        Map<String, Object> summary = adminService.getResourceSummary();
        return convertMapToJson(summary);
    }
    
    public String handleEmergencyReport() {
        if (currentAdminSession == null) {
            return "{\"error\":\"Unauthorized - Please login first\"}";
        }
        Map<String, Object> report = adminService.getEmergencyReport();
        return convertMapToJson(report);
    }
    
    public String handleForceAllocation() {
        if (currentAdminSession == null) {
            return "{\"error\":\"Unauthorized - Please login first\"}";
        }
        var allocations = adminService.forceAllocation();
        return JsonUtil.toJson(allocations);
    }
    
    public String handleCreateAdmin(String body) {
        if (currentAdminSession == null) {
            return "{\"error\":\"Unauthorized - Please login first\"}";
        }
        Map<String, Object> data = JsonUtil.parseJson(body);
        System.out.println("Admin create endpoint hit with body: " + body);
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        String role = (String) data.get("role");
        System.out.println("Parsed admin creation data - Username: " + username + ", Role: " + role);
        if (role == null) role = "ADMIN";
        
        var admin = adminService.createAdmin(username, password, role);
        return String.format("{\"status\":\"success\",\"message\":\"Admin %s created\",\"id\":\"%s\"}", 
                           username, admin.getId());
    }
    
    private String convertStatsToJson(SystemStats stats) {
        return String.format(
            "{\"totalHospitals\":%d,\"totalVendors\":%d,\"totalAllocations\":%d,\"status\":\"%s\",\"lastAllocationTime\":%d,\"totalDemand\":%s,\"totalInventory\":%s,\"totalShortage\":%s}",
            stats.getTotalHospitals(),
            stats.getTotalVendors(),
            stats.getTotalAllocations(),
            stats.getStatus(),
            stats.getLastAllocationTime(),
            mapToJson(stats.getTotalDemand()),
            mapToJson(stats.getTotalInventory()),
            mapToJson(stats.getTotalShortage())
        );
    }
    
    private String mapToJson(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append(String.format("\"%s\":%d", entry.getKey(), entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
    
    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append(String.format("\"%s\":", entry.getKey()));
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> nestedMap = (Map<String, Integer>) value;
                sb.append(mapToJson(nestedMap));
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof String) {
                sb.append(String.format("\"%s\"", value));
            } else if (value instanceof Boolean) {
                sb.append(value);
            } else if (value == null) {
                sb.append("null");
            } else {
                sb.append(String.format("\"%s\"", value.toString()));
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}