package service;

import model.*;
import repository.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminService {
    private final AdminRepository adminRepository;
    private final HospitalService hospitalService;
    private final VendorService vendorService;
    private final AllocationService allocationService;
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    
    public AdminService(AdminRepository adminRepository,
                        HospitalService hospitalService,
                        VendorService vendorService,
                        AllocationService allocationService) {
        this.adminRepository = adminRepository;
        this.hospitalService = hospitalService;
        this.vendorService = vendorService;
        this.allocationService = allocationService;
        
        // Create default admin account
        createDefaultAdmin();
    }
    
    private void createDefaultAdmin() {
        String id = String.valueOf(idGenerator.getAndIncrement());
        Admin defaultAdmin = new Admin(id, "admin", "admin123", "SUPER_ADMIN");
        adminRepository.save(defaultAdmin);
        System.out.println("Default admin created - Username: admin, Password: admin123");
    }
    
    public Admin createAdmin(String username, String password, String role) {
        String id = String.valueOf(idGenerator.getAndIncrement());
        Admin admin = new Admin(id, username, password, role);
        adminRepository.save(admin);
        return admin;
    }
    
    public Optional<Admin> login(String username, String password) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent() && adminOpt.get().getPassword().equals(password)) {
            Admin admin = adminOpt.get();
            admin.setLastLogin(new Date().toString());
            return Optional.of(admin);
        }
        return Optional.empty();
    }
    
    public SystemStats getSystemStats() {
        SystemStats stats = new SystemStats();
        
        // Count hospitals and vendors
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        List<Vendor> vendors = vendorService.getAllVendors();
        List<Allocation> allocations = allocationService.getAllAllocations();
        
        stats.setTotalHospitals(hospitals.size());
        stats.setTotalVendors(vendors.size());
        stats.setTotalAllocations(allocations.size());
        
        // Calculate total demand, inventory, and shortage
        Map<String, Integer> totalDemand = new HashMap<>();
        Map<String, Integer> totalInventory = new HashMap<>();
        
        for (Hospital hospital : hospitals) {
            for (Map.Entry<String, Integer> entry : hospital.getDemand().entrySet()) {
                String resource = entry.getKey();
                int demand = entry.getValue();
                totalDemand.put(resource, totalDemand.getOrDefault(resource, 0) + demand);
            }
            
            for (Map.Entry<String, Integer> entry : hospital.getInventory().entrySet()) {
                String resource = entry.getKey();
                int inventory = entry.getValue();
                totalInventory.put(resource, totalInventory.getOrDefault(resource, 0) + inventory);
            }
        }
        
        stats.setTotalDemand(totalDemand);
        stats.setTotalInventory(totalInventory);
        
        // Calculate shortage
        Map<String, Integer> totalShortage = new HashMap<>();
        for (Map.Entry<String, Integer> entry : totalDemand.entrySet()) {
            String resource = entry.getKey();
            int demand = entry.getValue();
            int inventory = totalInventory.getOrDefault(resource, 0);
            int shortage = Math.max(0, demand - inventory);
            totalShortage.put(resource, shortage);
        }
        stats.setTotalShortage(totalShortage);
        
        // Get last allocation time
        if (!allocations.isEmpty()) {
            stats.setLastAllocationTime(allocations.get(allocations.size() - 1).getTimestamp());
        }
        
        // Determine system status
        boolean hasShortage = totalShortage.values().stream().anyMatch(s -> s > 0);
        stats.setStatus(hasShortage ? "CRITICAL - Shortage Detected" : "HEALTHY");
        
        return stats;
    }
    
    public Map<String, Object> getResourceSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        List<Vendor> vendors = vendorService.getAllVendors();
        
        Map<String, Integer> totalVendorInventory = new HashMap<>();
        Map<String, Integer> totalHospitalDemand = new HashMap<>();
        Map<String, Integer> totalHospitalInventory = new HashMap<>();
        
        for (Vendor vendor : vendors) {
            for (Map.Entry<String, Integer> entry : vendor.getInventory().entrySet()) {
                String resource = entry.getKey();
                int qty = entry.getValue();
                totalVendorInventory.put(resource, totalVendorInventory.getOrDefault(resource, 0) + qty);
            }
        }
        
        for (Hospital hospital : hospitals) {
            for (Map.Entry<String, Integer> entry : hospital.getDemand().entrySet()) {
                String resource = entry.getKey();
                int demand = entry.getValue();
                totalHospitalDemand.put(resource, totalHospitalDemand.getOrDefault(resource, 0) + demand);
            }
            
            for (Map.Entry<String, Integer> entry : hospital.getInventory().entrySet()) {
                String resource = entry.getKey();
                int inventory = entry.getValue();
                totalHospitalInventory.put(resource, totalHospitalInventory.getOrDefault(resource, 0) + inventory);
            }
        }
        
        summary.put("totalVendorInventory", totalVendorInventory);
        summary.put("totalHospitalDemand", totalHospitalDemand);
        summary.put("totalHospitalInventory", totalHospitalInventory);
        
        return summary;
    }
    
    public Map<String, Object> getEmergencyReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        List<Vendor> vendors = vendorService.getAllVendors();
        
        // Critical hospitals (urgency > 7)
        List<Map<String, Object>> criticalHospitals = new ArrayList<>();
        for (Hospital hospital : hospitals) {
            if (hospital.getUrgencyLevel() > 7) {
                Map<String, Object> critical = new LinkedHashMap<>();
                critical.put("id", hospital.getId());
                critical.put("name", hospital.getName());
                critical.put("urgencyLevel", hospital.getUrgencyLevel());
                critical.put("demand", new HashMap<>(hospital.getDemand()));
                critical.put("inventory", new HashMap<>(hospital.getInventory()));
                
                // Calculate shortage
                Map<String, Integer> shortage = new HashMap<>();
                for (Map.Entry<String, Integer> entry : hospital.getDemand().entrySet()) {
                    String resource = entry.getKey();
                    int demand = entry.getValue();
                    int inventory = hospital.getInventory().getOrDefault(resource, 0);
                    if (inventory < demand) {
                        shortage.put(resource, demand - inventory);
                    }
                }
                critical.put("shortage", shortage);
                criticalHospitals.add(critical);
            }
        }
        
        // Low inventory vendors (inventory < 20 units)
        List<Map<String, Object>> lowInventoryVendors = new ArrayList<>();
        for (Vendor vendor : vendors) {
            Map<String, Integer> lowStock = new HashMap<>();
            for (Map.Entry<String, Integer> entry : vendor.getInventory().entrySet()) {
                if (entry.getValue() < 20) {
                    lowStock.put(entry.getKey(), entry.getValue());
                }
            }
            if (!lowStock.isEmpty()) {
                Map<String, Object> vendorReport = new LinkedHashMap<>();
                vendorReport.put("id", vendor.getId());
                vendorReport.put("name", vendor.getName());
                vendorReport.put("lowInventory", lowStock);
                lowInventoryVendors.add(vendorReport);
            }
        }
        
        report.put("criticalHospitals", criticalHospitals);
        report.put("lowInventoryVendors", lowInventoryVendors);
        report.put("totalHospitals", hospitals.size());
        report.put("totalVendors", vendors.size());
        report.put("timestamp", System.currentTimeMillis());
        
        return report;
    }
    
    public List<Allocation> forceAllocation() {
        // Force run allocation regardless of conditions
        System.out.println("!!! EMERGENCY ALLOCATION FORCED BY ADMIN !!!");
        return allocationService.runAllocation();
    }
    
    public void reset() {
        adminRepository.deleteAll();
        idGenerator.set(1);
        createDefaultAdmin(); // Recreate default admin
    }
}