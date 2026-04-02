// controller/AllocationController.java
package controller;

import service.AllocationService;
import util.JsonUtil;
import java.util.List;
import model.Allocation;

public class AllocationController {
    private final AllocationService allocationService;
    
    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }
    
    public String handlePost() {
        List<Allocation> allocations = allocationService.runAllocation();
        return JsonUtil.toJson(allocations);
    }
    
    public String handleGet() {
        var allocations = allocationService.getAllAllocations();
        return JsonUtil.toJson(allocations);
    }
}