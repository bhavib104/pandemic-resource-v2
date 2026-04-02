// service/CitizenService.java
package service;

import model.Hospital;
import model.Vendor;
import util.DistanceCalculator;
import java.util.*;
import java.util.stream.Collectors;

public class CitizenService {
    private final HospitalService hospitalService;
    private final VendorService vendorService;
    
    public CitizenService(HospitalService hospitalService, VendorService vendorService) {
        this.hospitalService = hospitalService;
        this.vendorService = vendorService;
    }
    
    public List<Map<String, Object>> getNearestHospitals(double lat, double lon, int limit) {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        
        return hospitals.stream()
            .map(hospital -> {
                double distance = DistanceCalculator.calculateDistance(
                    lat, lon,
                    hospital.getLatitude(), hospital.getLongitude()
                );
                Map<String, Object> result = new HashMap<>();
                result.put("id", hospital.getId());
                result.put("name", hospital.getName());
                result.put("distance_km", distance);
                result.put("urgencyLevel", hospital.getUrgencyLevel());
                result.put("demand", hospital.getDemand());
                result.put("inventory", hospital.getInventory());
                return result;
            })
            .sorted((h1, h2) -> Double.compare(
                (Double) h1.get("distance_km"), 
                (Double) h2.get("distance_km")
            ))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getNearestHospitals(double lat, double lon) {
        return getNearestHospitals(lat, lon, 10); // Default to top 10
    }
    
    public List<Map<String, Object>> getResourceAvailability(double lat, double lon, String resourceType) {
        List<Map<String, Object>> availability = new ArrayList<>();
        
        // Check hospitals
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        for (Hospital hospital : hospitals) {
            int available = hospital.getInventory().getOrDefault(resourceType, 0);
            int demand = hospital.getDemand().getOrDefault(resourceType, 0);
            double distance = DistanceCalculator.calculateDistance(
                lat, lon,
                hospital.getLatitude(), hospital.getLongitude()
            );
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", "hospital");
            entry.put("name", hospital.getName());
            entry.put("id", hospital.getId());
            entry.put("available", available);
            entry.put("demand", demand);
            entry.put("shortage", Math.max(0, demand - available));
            entry.put("distance_km", distance);
            availability.add(entry);
        }
        
        // Check vendors
        List<Vendor> vendors = vendorService.getAllVendors();
        for (Vendor vendor : vendors) {
            int available = vendor.getInventory().getOrDefault(resourceType, 0);
            double distance = DistanceCalculator.calculateDistance(
                lat, lon,
                vendor.getLatitude(), vendor.getLongitude()
            );
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", "vendor");
            entry.put("name", vendor.getName());
            entry.put("id", vendor.getId());
            entry.put("available", available);
            entry.put("distance_km", distance);
            availability.add(entry);
        }
        
        // Sort by distance
        availability.sort((a, b) -> Double.compare(
            (Double) a.get("distance_km"), 
            (Double) b.get("distance_km")
        ));
        
        return availability;
    }
    
    public Map<String, Object> getAggregatedResourceAvailability(String resourceType) {
        Map<String, Object> aggregated = new HashMap<>();
        
        int totalHospitalInventory = 0;
        int totalHospitalDemand = 0;
        int totalVendorInventory = 0;
        
        for (Hospital hospital : hospitalService.getAllHospitals()) {
            totalHospitalInventory += hospital.getInventory().getOrDefault(resourceType, 0);
            totalHospitalDemand += hospital.getDemand().getOrDefault(resourceType, 0);
        }
        
        for (Vendor vendor : vendorService.getAllVendors()) {
            totalVendorInventory += vendor.getInventory().getOrDefault(resourceType, 0);
        }
        
        aggregated.put("resourceType", resourceType);
        aggregated.put("totalHospitalInventory", totalHospitalInventory);
        aggregated.put("totalHospitalDemand", totalHospitalDemand);
        aggregated.put("totalVendorInventory", totalVendorInventory);
        aggregated.put("totalAvailableSupply", totalHospitalInventory + totalVendorInventory);
        aggregated.put("totalShortage", Math.max(0, totalHospitalDemand - (totalHospitalInventory + totalVendorInventory)));
        
        return aggregated;
    }
}