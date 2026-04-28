package model;

import java.util.*;

public class SystemStats {
    private int totalHospitals;
    private int totalVendors;
    private int totalAllocations;
    private Map<String, Integer> totalDemand;
    private Map<String, Integer> totalInventory;
    private Map<String, Integer> totalShortage;
    private long lastAllocationTime;
    private String status;
    
    public SystemStats() {
        this.totalDemand = new HashMap<>();
        this.totalInventory = new HashMap<>();
        this.totalShortage = new HashMap<>();
        this.status = "ACTIVE";
        this.totalHospitals = 0;
        this.totalVendors = 0;
        this.totalAllocations = 0;
        this.lastAllocationTime = 0;
    }
    
    // Getters
    public int getTotalHospitals() { 
        return totalHospitals; 
    }
    
    public int getTotalVendors() { 
        return totalVendors; 
    }
    
    public int getTotalAllocations() { 
        return totalAllocations; 
    }
    
    public Map<String, Integer> getTotalDemand() { 
        return totalDemand; 
    }
    
    public Map<String, Integer> getTotalInventory() { 
        return totalInventory; 
    }
    
    public Map<String, Integer> getTotalShortage() { 
        return totalShortage; 
    }
    
    public long getLastAllocationTime() { 
        return lastAllocationTime; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    // Setters
    public void setTotalHospitals(int totalHospitals) { 
        this.totalHospitals = totalHospitals; 
    }
    
    public void setTotalVendors(int totalVendors) { 
        this.totalVendors = totalVendors; 
    }
    
    public void setTotalAllocations(int totalAllocations) { 
        this.totalAllocations = totalAllocations; 
    }
    
    public void setTotalDemand(Map<String, Integer> totalDemand) { 
        this.totalDemand = totalDemand; 
    }
    
    public void setTotalInventory(Map<String, Integer> totalInventory) { 
        this.totalInventory = totalInventory; 
    }
    
    public void setTotalShortage(Map<String, Integer> totalShortage) { 
        this.totalShortage = totalShortage; 
    }
    
    public void setLastAllocationTime(long lastAllocationTime) { 
        this.lastAllocationTime = lastAllocationTime; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
}