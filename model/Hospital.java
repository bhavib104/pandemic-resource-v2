// model/Hospital.java
package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Hospital {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private int urgencyLevel; // 1-10, where 10 is most urgent
    private Map<String, Integer> demand;
    private Map<String, Integer> inventory;
    
    public Hospital(String id, String name, double latitude, double longitude, int urgencyLevel) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.urgencyLevel = urgencyLevel;
        this.demand = new ConcurrentHashMap<>();
        this.inventory = new ConcurrentHashMap<>();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(int urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public Map<String, Integer> getDemand() { return demand; }
    public Map<String, Integer> getInventory() { return inventory; }
}