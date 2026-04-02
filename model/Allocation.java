// model/Allocation.java
package model;

public class Allocation {
    private String hospitalId;
    private String vendorId;
    private String resourceType;
    private int quantity;
    private double distance;
    private double priorityScore;
    private long timestamp;
    
    public Allocation(String hospitalId, String vendorId, String resourceType, 
                     int quantity, double distance, double priorityScore) {
        this.hospitalId = hospitalId;
        this.vendorId = vendorId;
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.distance = distance;
        this.priorityScore = priorityScore;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getHospitalId() { return hospitalId; }
    public String getVendorId() { return vendorId; }
    public String getResourceType() { return resourceType; }
    public int getQuantity() { return quantity; }
    public double getDistance() { return distance; }
    public double getPriorityScore() { return priorityScore; }
    public long getTimestamp() { return timestamp; }
}