// service/AllocationService.java
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
        // Clear previous allocations
        allocationRepository.deleteAll();
        
        // Get current hospitals and vendors
        var hospitals = hospitalService.getAllHospitals();
        var vendors = vendorService.getAllVendors();
        
        // Run allocation engine
        List<Allocation> allocations = allocationEngine.executeAllocation(hospitals, vendors);
        
        // Save allocations
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