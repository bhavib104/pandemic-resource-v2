// engine/AllocationEngine.java
package engine;

import model.*;
import util.DistanceCalculator;
import java.util.*;
import java.util.stream.Collectors;

public class AllocationEngine {
    private static final double URGENCY_WEIGHT = 0.5;
    private static final double DISTANCE_WEIGHT = 0.3;
    private static final double AVAILABILITY_WEIGHT = 0.2;
    private static final double MIN_ALLOCATION_RATIO = 0.1; // Each hospital gets at least 10% of demand if possible
    
    public List<Allocation> executeAllocation(List<Hospital> hospitals, List<Vendor> vendors) {
        List<Allocation> allocations = new ArrayList<>();
        
        if (vendors.isEmpty()) {
            return allocations;
        }
        
        // Create working copies
        List<Hospital> workingHospitals = new ArrayList<>(hospitals);
        Map<String, Map<String, Integer>> vendorInventory = new HashMap<>();
        
        for (Vendor vendor : vendors) {
            vendorInventory.put(vendor.getId(), new HashMap<>(vendor.getInventory()));
        }
        
        // Sort hospitals by urgency (highest first)
        workingHospitals.sort((h1, h2) -> Integer.compare(h2.getUrgencyLevel(), h1.getUrgencyLevel()));
        
        // First pass: Ensure minimum allocation for each hospital
        ensureMinimumAllocation(workingHospitals, vendors, vendorInventory, allocations);
        
        // Second pass: Full allocation based on priority scores
        for (Hospital hospital : workingHospitals) {
            allocateForHospital(hospital, vendors, vendorInventory, allocations);
        }
        
        return allocations;
    }
    
    private void ensureMinimumAllocation(List<Hospital> hospitals, List<Vendor> vendors, 
                                         Map<String, Map<String, Integer>> vendorInventory,
                                         List<Allocation> allocations) {
        for (Hospital hospital : hospitals) {
            for (Map.Entry<String, Integer> demandEntry : hospital.getDemand().entrySet()) {
                String resourceType = demandEntry.getKey();
                int demand = demandEntry.getValue();
                int minRequired = (int) Math.ceil(demand * MIN_ALLOCATION_RATIO);
                int allocated = 0;
                
                // Find vendors sorted by distance
                List<Vendor> sortedVendors = getVendorsSortedByDistance(hospital, vendors);
                
                for (Vendor vendor : sortedVendors) {
                    if (allocated >= minRequired) break;
                    
                    int available = vendorInventory.get(vendor.getId()).getOrDefault(resourceType, 0);
                    if (available > 0) {
                        int toAllocate = Math.min(minRequired - allocated, available);
                        if (toAllocate > 0) {
                            // Calculate priority score for minimum allocation
                            double distance = DistanceCalculator.calculateDistance(
                                hospital.getLatitude(), hospital.getLongitude(),
                                vendor.getLatitude(), vendor.getLongitude()
                            );
                            double maxDistance = 1000.0; // Normalization factor
                            double inverseDistance = 1.0 / (1.0 + distance / maxDistance);
                            double availabilityRatio = (double) toAllocate / demand;
                            double priorityScore = (URGENCY_WEIGHT * hospital.getUrgencyLevel() / 10.0) +
                                                  (DISTANCE_WEIGHT * inverseDistance) +
                                                  (AVAILABILITY_WEIGHT * availabilityRatio);
                            
                            allocations.add(new Allocation(
                                hospital.getId(), vendor.getId(), resourceType,
                                toAllocate, distance, priorityScore
                            ));
                            
                            // Update inventories
                            vendorInventory.get(vendor.getId()).put(resourceType, available - toAllocate);
                            allocated += toAllocate;
                        }
                    }
                }
            }
        }
    }
    
    private void allocateForHospital(Hospital hospital, List<Vendor> vendors,
                                    Map<String, Map<String, Integer>> vendorInventory,
                                    List<Allocation> allocations) {
        for (Map.Entry<String, Integer> demandEntry : hospital.getDemand().entrySet()) {
            String resourceType = demandEntry.getKey();
            int remainingDemand = demandEntry.getValue();
            
            // Track already allocated for this hospital-resource pair
            int alreadyAllocated = allocations.stream()
                .filter(a -> a.getHospitalId().equals(hospital.getId()) && 
                            a.getResourceType().equals(resourceType))
                .mapToInt(Allocation::getQuantity)
                .sum();
            
            remainingDemand -= alreadyAllocated;
            if (remainingDemand <= 0) continue;
            
            // Get vendors with this resource, sorted by priority score
            List<VendorScore> vendorScores = new ArrayList<>();
            
            for (Vendor vendor : vendors) {
                int available = vendorInventory.get(vendor.getId()).getOrDefault(resourceType, 0);
                if (available > 0) {
                    double distance = DistanceCalculator.calculateDistance(
                        hospital.getLatitude(), hospital.getLongitude(),
                        vendor.getLatitude(), vendor.getLongitude()
                    );
                    
                    double maxDistance = 1000.0;
                    double inverseDistance = 1.0 / (1.0 + distance / maxDistance);
                    double availabilityRatio = Math.min(1.0, (double) available / remainingDemand);
                    double urgencyRatio = hospital.getUrgencyLevel() / 10.0;
                    
                    double priorityScore = (URGENCY_WEIGHT * urgencyRatio) +
                                          (DISTANCE_WEIGHT * inverseDistance) +
                                          (AVAILABILITY_WEIGHT * availabilityRatio);
                    
                    vendorScores.add(new VendorScore(vendor, available, distance, priorityScore));
                }
            }
            
            // Sort by priority score (descending)
            vendorScores.sort((vs1, vs2) -> Double.compare(vs2.priorityScore, vs1.priorityScore));
            
            // Allocate
            for (VendorScore vs : vendorScores) {
                if (remainingDemand <= 0) break;
                
                int toAllocate = Math.min(remainingDemand, vs.available);
                if (toAllocate > 0) {
                    allocations.add(new Allocation(
                        hospital.getId(), vs.vendor.getId(), resourceType,
                        toAllocate, vs.distance, vs.priorityScore
                    ));
                    
                    // Update inventory
                    vendorInventory.get(vs.vendor.getId()).put(resourceType, vs.available - toAllocate);
                    remainingDemand -= toAllocate;
                }
            }
        }
    }
    
    private List<Vendor> getVendorsSortedByDistance(Hospital hospital, List<Vendor> vendors) {
        return vendors.stream()
            .sorted((v1, v2) -> {
                double dist1 = DistanceCalculator.calculateDistance(
                    hospital.getLatitude(), hospital.getLongitude(),
                    v1.getLatitude(), v1.getLongitude()
                );
                double dist2 = DistanceCalculator.calculateDistance(
                    hospital.getLatitude(), hospital.getLongitude(),
                    v2.getLatitude(), v2.getLongitude()
                );
                return Double.compare(dist1, dist2);
            })
            .collect(Collectors.toList());
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