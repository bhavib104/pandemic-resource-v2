// model/Vendor.java
package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Vendor {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private Map<String, Integer> inventory;
    
    public Vendor(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
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
    public Map<String, Integer> getInventory() { return inventory; }
}