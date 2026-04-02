// util/JsonUtil.java
package util;

import java.util.*;
import model.*;

public class JsonUtil {
    
    public static String toJson(Hospital hospital) {
        return String.format("{\"id\":\"%s\",\"name\":\"%s\",\"latitude\":%f,\"longitude\":%f,\"urgencyLevel\":%d,\"demand\":%s,\"inventory\":%s}",
            hospital.getId(), hospital.getName(), hospital.getLatitude(), 
            hospital.getLongitude(), hospital.getUrgencyLevel(),
            mapToJson(hospital.getDemand()), mapToJson(hospital.getInventory()));
    }
    
    public static String toJson(Vendor vendor) {
        return String.format("{\"id\":\"%s\",\"name\":\"%s\",\"latitude\":%f,\"longitude\":%f,\"inventory\":%s}",
            vendor.getId(), vendor.getName(), vendor.getLatitude(), 
            vendor.getLongitude(), mapToJson(vendor.getInventory()));
    }
    
    public static String toJson(Allocation allocation) {
        return String.format("{\"hospitalId\":\"%s\",\"vendorId\":\"%s\",\"resourceType\":\"%s\",\"quantity\":%d,\"distance\":%f,\"priorityScore\":%f,\"timestamp\":%d}",
            allocation.getHospitalId(), allocation.getVendorId(), allocation.getResourceType(),
            allocation.getQuantity(), allocation.getDistance(), allocation.getPriorityScore(),
            allocation.getTimestamp());
    }
    
    public static String toJson(List<?> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            if (items.get(i) instanceof Hospital) {
                sb.append(toJson((Hospital) items.get(i)));
            } else if (items.get(i) instanceof Vendor) {
                sb.append(toJson((Vendor) items.get(i)));
            } else if (items.get(i) instanceof Allocation) {
                sb.append(toJson((Allocation) items.get(i)));
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String mapToJson(Map<String, Integer> map) {
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
    
    public static Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return result;
        
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                    String value = keyValue[1].trim();
                    
                    if (value.startsWith("\"")) {
                        result.put(key, value.replaceAll("^\"|\"$", ""));
                    } else if (value.equals("true") || value.equals("false")) {
                        result.put(key, Boolean.parseBoolean(value));
                    } else if (value.contains(".")) {
                        result.put(key, Double.parseDouble(value));
                    } else {
                        try {
                            result.put(key, Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            result.put(key, value);
                        }
                    }
                }
            }
        }
        return result;
    }
}