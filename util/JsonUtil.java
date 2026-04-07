package util;

import model.*;
import java.util.*;

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
    
    public static String toJson(java.util.List<?> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            Object item = items.get(i);
            if (item instanceof Hospital) {
                sb.append(toJson((Hospital) item));
            } else if (item instanceof Vendor) {
                sb.append(toJson((Vendor) item));
            } else if (item instanceof Allocation) {
                sb.append(toJson((Allocation) item));
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String mapToJson(Map<String, Integer> map) {
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
    
    public static Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return result;
        
        String trimmedJson = json.trim();
        if (!trimmedJson.startsWith("{") || !trimmedJson.endsWith("}")) return result;
        
        String content = trimmedJson.substring(1, trimmedJson.length() - 1);
        if (content.trim().isEmpty()) return result;
        
        String[] pairs = content.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                
                if (value.startsWith("{") && value.endsWith("}")) {
                    // Handle nested object
                    Map<String, Object> nestedResult = new HashMap<>();
                    String nestedContent = value.substring(1, value.length() - 1);
                    String[] nestedPairs = nestedContent.split(",");
                    for (String nestedPair : nestedPairs) {
                        String[] nestedKeyValue = nestedPair.split(":", 2);
                        if (nestedKeyValue.length == 2) {
                            String nestedKey = nestedKeyValue[0].trim().replaceAll("\"", "");
                            String nestedValue = nestedKeyValue[1].trim().replaceAll("\"", "");
                            try {
                                if (nestedValue.contains(".")) {
                                    nestedResult.put(nestedKey, Double.parseDouble(nestedValue));
                                } else {
                                    nestedResult.put(nestedKey, Integer.parseInt(nestedValue));
                                }
                            } catch (NumberFormatException e) {
                                nestedResult.put(nestedKey, nestedValue);
                            }
                        }
                    }
                    result.put(key, nestedResult);
                } else {
                    try {
                        if (value.contains(".")) {
                            result.put(key, Double.parseDouble(value));
                        } else {
                            result.put(key, Integer.parseInt(value));
                        }
                    } catch (NumberFormatException e) {
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }
    
    public static Map<String, Integer> extractMap(Map<String, Object> jsonMap, String key) {
        Map<String, Integer> result = new HashMap<>();
        
        if (key == null) {
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    result.put(entry.getKey(), ((Number) value).intValue());
                }
            }
            return result;
        }
        
        Object obj = jsonMap.get(key);
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : nestedMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    result.put(entry.getKey(), ((Number) value).intValue());
                }
            }
        }
        return result;
    }
}