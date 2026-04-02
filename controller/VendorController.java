// controller/VendorController.java
package controller;

import service.VendorService;
import util.JsonUtil;
// import util.RequestParser;
import java.util.Map;

public class VendorController {
    private final VendorService vendorService;
    
    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    
    public String handlePost(String body) {
        Map<String, Object> data = JsonUtil.parseJson(body);
        
        String name = (String) data.get("name");
        double lat = ((Number) data.get("latitude")).doubleValue();
        double lon = ((Number) data.get("longitude")).doubleValue();
        
        var vendor = vendorService.createVendor(name, lat, lon);
        return JsonUtil.toJson(vendor);
    }
    
    public String handleGet() {
        var vendors = vendorService.getAllVendors();
        return JsonUtil.toJson(vendors);
    }
    
    public String handlePutInventory(String id, String body) {
        Map<String, Object> data = JsonUtil.parseJson(body);
        Map<String, Integer> inventory = new java.util.HashMap<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            inventory.put(entry.getKey(), ((Number) entry.getValue()).intValue());
        }
        
        boolean success = vendorService.updateInventory(id, inventory);
        if (success) {
            return "{\"status\":\"success\",\"message\":\"Inventory updated\"}";
        }
        return "{\"status\":\"error\",\"message\":\"Vendor not found\"}";
    }
}