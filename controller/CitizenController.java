// controller/CitizenController.java (Fixed)
package controller;

import service.CitizenService;
// import util.JsonUtil;
import java.util.Map;
import java.util.List;

public class CitizenController {
    private final CitizenService citizenService;
    
    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }
    
    public String handleNearestHospitals(Map<String, String> params) {
        try {
            double lat = Double.parseDouble(params.get("lat"));
            double lon = Double.parseDouble(params.get("lon"));
            List<Map<String, Object>> hospitals = citizenService.getNearestHospitals(lat, lon);
            return convertToJson(hospitals);
        } catch (Exception e) {
            return String.format("{\"error\":\"Invalid parameters: %s\"}", e.getMessage());
        }
    }
    
    public String handleResourceAvailability(Map<String, String> params) {
        try {
            double lat = Double.parseDouble(params.get("lat"));
            double lon = Double.parseDouble(params.get("lon"));
            String resourceType = params.get("type");
            
            if (resourceType == null || resourceType.isEmpty()) {
                return "{\"error\":\"Resource type is required\"}";
            }
            
            List<Map<String, Object>> availability = citizenService.getResourceAvailability(lat, lon, resourceType);
            return convertToJson(availability);
        } catch (Exception e) {
            return String.format("{\"error\":\"Invalid parameters: %s\"}", e.getMessage());
        }
    }
    
    private String convertToJson(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append(String.format("\"%s\":", entry.getKey()));
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append(String.format("\"%s\"", value));
            } else if (value instanceof Number) {
                sb.append(value);
            } else if (value instanceof Map) {
                sb.append(mapToJson((Map<String, Object>) value));
            } else {
                sb.append(String.format("\"%s\"", value));
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}