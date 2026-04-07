package controller;

import service.AllocationService;
import util.JsonUtil;

public class AllocationController {
    private final AllocationService allocationService;
    
    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }
    
    public String handlePost() {
        var allocations = allocationService.runAllocation();
        // After allocation, the hospitals should have updated inventory
        return JsonUtil.toJson(allocations);
    }
    
    public String handleGet() {
        var allocations = allocationService.getAllAllocations();
        return JsonUtil.toJson(allocations);
    }
}