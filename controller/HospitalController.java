// controller/HospitalController.java
package controller;

import service.HospitalService;
import util.JsonUtil;
// import util.RequestParser;
import java.util.Map;

public class HospitalController {
    private final HospitalService hospitalService;
    
    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }
    
    public String handlePost(String body) {
        Map<String, Object> data = JsonUtil.parseJson(body);
        
        String name = (String) data.get("name");
        double lat = ((Number) data.get("latitude")).doubleValue();
        double lon = ((Number) data.get("longitude")).doubleValue();
        int urgency = ((Number) data.get("urgencyLevel")).intValue();
        
        var hospital = hospitalService.createHospital(name, lat, lon, urgency);
        return JsonUtil.toJson(hospital);
    }
    
    public String handleGet() {
        var hospitals = hospitalService.getAllHospitals();
        return JsonUtil.toJson(hospitals);
    }
    
    public String handlePutDemand(String id, String body) {
        Map<String, Object> data = JsonUtil.parseJson(body);
        Map<String, Integer> demand = new java.util.HashMap<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            demand.put(entry.getKey(), ((Number) entry.getValue()).intValue());
        }
        
        boolean success = hospitalService.updateDemand(id, demand);
        if (success) {
            return "{\"status\":\"success\",\"message\":\"Demand updated\"}";
        }
        return "{\"status\":\"error\",\"message\":\"Hospital not found\"}";
    }
}