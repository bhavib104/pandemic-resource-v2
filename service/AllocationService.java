package service;

import model.Allocation;
import repository.AllocationRepository;
import engine.AllocationEngine;
import java.util.List;

public class AllocationService {
    private final AllocationRepository allocationRepository;
    private final HospitalService hospitalService;
    private final VendorService vendorService;
    private final AllocationEngine allocationEngine;
    
    public AllocationService(AllocationRepository allocationRepository,
                            HospitalService hospitalService,
                            VendorService vendorService,
                            AllocationEngine allocationEngine) {
        this.allocationRepository = allocationRepository;
        this.hospitalService = hospitalService;
        this.vendorService = vendorService;
        this.allocationEngine = allocationEngine;
    }
    
    public List<Allocation> runAllocation() {
        allocationRepository.deleteAll();
        var hospitals = hospitalService.getAllHospitals();
        var vendors = vendorService.getAllVendors();
        
        System.out.println("=== Allocation Service ===");
        System.out.println("Hospitals before allocation: " + hospitals.size());
        
        List<Allocation> allocations = allocationEngine.executeAllocation(hospitals, vendors);
        
        System.out.println("Hospitals after allocation: " + hospitals.size());
        for (var h : hospitals) {
            System.out.println("Hospital " + h.getId() + " inventory: " + h.getInventory());
        }
        
        allocationRepository.saveAll(allocations);
        return allocations;
    }
    
    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAll();
    }
    
    public void reset() {
        allocationRepository.deleteAll();
    }
}