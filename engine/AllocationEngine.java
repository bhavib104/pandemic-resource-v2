package engine;

import model.*;
import util.DistanceCalculator;
import java.util.*;

public class AllocationEngine {
    private static final double URGENCY_WEIGHT = 0.5;
    private static final double DISTANCE_WEIGHT = 0.3;
    private static final double AVAILABILITY_WEIGHT = 0.2;
    private static final double MIN_ALLOCATION_RATIO = 0.1;
    
    public List<Allocation> executeAllocation(List<Hospital> hospitals, List<Vendor> vendors) {
        List<Allocation> allocations = new ArrayList<>();
        
        System.out.println("\n========== ALLOCATION ENGINE START ==========");
        System.out.println("Hospitals count: " + hospitals.size());
        System.out.println("Vendors count: " + vendors.size());
        
        if (hospitals.isEmpty() || vendors.isEmpty()) {
            System.out.println("No hospitals or vendors - exiting");
            return allocations;
        }
        
        // Print initial state
        for (Hospital h : hospitals) {
            System.out.println("Hospital " + h.getId() + " demand: " + h.getDemand());
            System.out.println("Hospital " + h.getId() + " inventory BEFORE: " + h.getInventory());
        }
        
        // Create working copy of vendor inventory
        Map<String, Map<String, Integer>> vendorInventory = new HashMap<>();
        for (Vendor vendor : vendors) {
            vendorInventory.put(vendor.getId(), new HashMap<>(vendor.getInventory()));
            System.out.println("Vendor " + vendor.getId() + " inventory: " + vendor.getInventory());
        }
        
        // Sort hospitals by urgency
        List<Hospital> sortedHospitals = new ArrayList<>(hospitals);
        sortedHospitals.sort((h1, h2) -> Integer.compare(h2.getUrgencyLevel(), h1.getUrgencyLevel()));
        
        // FIRST PASS: Minimum allocation (10%)
        System.out.println("\n--- FIRST PASS: Minimum Allocation (10%) ---");
        for (Hospital hospital : sortedHospitals) {
            System.out.println("\nProcessing Hospital " + hospital.getId() + " (Urgency: " + hospital.getUrgencyLevel() + ")");
            
            for (Map.Entry<String, Integer> demandEntry : hospital.getDemand().entrySet()) {
                String resourceType = demandEntry.getKey();
                int demand = demandEntry.getValue();
                int minRequired = (int) Math.ceil(demand * MIN_ALLOCATION_RATIO);
                
                System.out.println("  Resource: " + resourceType + ", Demand: " + demand + ", Min Required: " + minRequired);
                
                int allocated = 0;
                
                // Get vendors sorted by distance
                List<Vendor> sortedVendors = new ArrayList<>(vendors);
                sortedVendors.sort((v1, v2) -> {
                    double d1 = DistanceCalculator.calculateDistance(
                        hospital.getLatitude(), hospital.getLongitude(),
                        v1.getLatitude(), v1.getLongitude()
                    );
                    double d2 = DistanceCalculator.calculateDistance(
                        hospital.getLatitude(), hospital.getLongitude(),
                        v2.getLatitude(), v2.getLongitude()
                    );
                    return Double.compare(d1, d2);
                });
                
                for (Vendor vendor : sortedVendors) {
                    if (allocated >= minRequired) break;
                    
                    int available = vendorInventory.get(vendor.getId()).getOrDefault(resourceType, 0);
                    if (available > 0) {
                        int toAllocate = Math.min(minRequired - allocated, available);
                        
                        System.out.println("    Vendor " + vendor.getId() + " has " + available + ", allocating " + toAllocate);
                        
                        double distance = DistanceCalculator.calculateDistance(
                            hospital.getLatitude(), hospital.getLongitude(),
                            vendor.getLatitude(), vendor.getLongitude()
                        );
                        
                        double priorityScore = 0.5 * (hospital.getUrgencyLevel() / 10.0) +
                                              0.3 * (1.0 / (1.0 + distance / 1000)) +
                                              0.2 * ((double) toAllocate / demand);
                        
                        allocations.add(new Allocation(
                            hospital.getId(), vendor.getId(), resourceType,
                            toAllocate, distance, priorityScore
                        ));
                        
                        // Update vendor inventory
                        vendorInventory.get(vendor.getId()).put(resourceType, available - toAllocate);
                        
                        // CRITICAL FIX: Update hospital inventory directly
                        int currentInv = hospital.getInventory().getOrDefault(resourceType, 0);
                        hospital.getInventory().put(resourceType, currentInv + toAllocate);
                        System.out.println("      Hospital " + hospital.getId() + " inventory now: " + hospital.getInventory());
                        
                        allocated += toAllocate;
                    }
                }
            }
        }
        
        // SECOND PASS: Full allocation based on priority
        System.out.println("\n--- SECOND PASS: Full Priority Allocation ---");
        for (Hospital hospital : sortedHospitals) {
            System.out.println("\nProcessing Hospital " + hospital.getId());
            
            for (Map.Entry<String, Integer> demandEntry : hospital.getDemand().entrySet()) {
                String resourceType = demandEntry.getKey();
                int totalDemand = demandEntry.getValue();
                
                // Calculate already allocated
                int alreadyAllocated = 0;
                for (Allocation a : allocations) {
                    if (a.getHospitalId().equals(hospital.getId()) && a.getResourceType().equals(resourceType)) {
                        alreadyAllocated += a.getQuantity();
                    }
                }
                
                int remainingDemand = totalDemand - alreadyAllocated;
                System.out.println("  Resource: " + resourceType + ", Total: " + totalDemand + ", Already: " + alreadyAllocated + ", Remaining: " + remainingDemand);
                
                if (remainingDemand <= 0) continue;
                
                // Calculate priority scores for vendors
                List<VendorScore> vendorScores = new ArrayList<>();
                
                for (Vendor vendor : vendors) {
                    int available = vendorInventory.get(vendor.getId()).getOrDefault(resourceType, 0);
                    if (available > 0) {
                        double distance = DistanceCalculator.calculateDistance(
                            hospital.getLatitude(), hospital.getLongitude(),
                            vendor.getLatitude(), vendor.getLongitude()
                        );
                        
                        double priorityScore = 0.5 * (hospital.getUrgencyLevel() / 10.0) +
                                              0.3 * (1.0 / (1.0 + distance / 1000)) +
                                              0.2 * (Math.min(1.0, (double) available / remainingDemand));
                        
                        vendorScores.add(new VendorScore(vendor, available, distance, priorityScore));
                    }
                }
                
                vendorScores.sort((a, b) -> Double.compare(b.priorityScore, a.priorityScore));
                
                int remainingToAllocate = remainingDemand;
                
                for (VendorScore vs : vendorScores) {
                    if (remainingToAllocate <= 0) break;
                    
                    int available = vendorInventory.get(vs.vendor.getId()).getOrDefault(resourceType, 0);
                    if (available <= 0) continue;
                    
                    int toAllocate = Math.min(remainingToAllocate, available);
                    
                    System.out.println("    Vendor " + vs.vendor.getId() + " allocating " + toAllocate + " (Score: " + vs.priorityScore + ")");
                    
                    allocations.add(new Allocation(
                        hospital.getId(), vs.vendor.getId(), resourceType,
                        toAllocate, vs.distance, vs.priorityScore
                    ));
                    
                    // Update vendor inventory
                    vendorInventory.get(vs.vendor.getId()).put(resourceType, available - toAllocate);
                    
                    // CRITICAL FIX: Update hospital inventory again
                    int currentInv = hospital.getInventory().getOrDefault(resourceType, 0);
                    hospital.getInventory().put(resourceType, currentInv + toAllocate);
                    System.out.println("      Hospital " + hospital.getId() + " inventory now: " + hospital.getInventory());
                    
                    remainingToAllocate -= toAllocate;
                }
            }
        }
        
        // Update actual vendor inventories
        for (Vendor vendor : vendors) {
            Map<String, Integer> updatedInv = vendorInventory.get(vendor.getId());
            if (updatedInv != null) {
                vendor.getInventory().clear();
                vendor.getInventory().putAll(updatedInv);
            }
        }
        
        // Print final state
        System.out.println("\n========== FINAL STATE ==========");
        for (Hospital h : hospitals) {
            System.out.println("Hospital " + h.getId() + " FINAL inventory: " + h.getInventory());
        }
        for (Vendor v : vendors) {
            System.out.println("Vendor " + v.getId() + " FINAL inventory: " + v.getInventory());
        }
        System.out.println("Total allocations: " + allocations.size());
        System.out.println("========== ALLOCATION ENGINE END ==========\n");
        
        return allocations;
    }
    
    private static class VendorScore {
        Vendor vendor;
        int available;
        double distance;
        double priorityScore;
        
        VendorScore(Vendor vendor, int available, double distance, double priorityScore) {
            this.vendor = vendor;
            this.available = available;
            this.distance = distance;
            this.priorityScore = priorityScore;
        }
    }
}