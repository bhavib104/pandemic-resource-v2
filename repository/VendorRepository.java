// repository/VendorRepository.java
package repository;

import model.Vendor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VendorRepository {
    private final Map<String, Vendor> vendors = new ConcurrentHashMap<>();
    
    public void save(Vendor vendor) {
        vendors.put(vendor.getId(), vendor);
    }
    
    public Optional<Vendor> findById(String id) {
        return Optional.ofNullable(vendors.get(id));
    }
    
    public List<Vendor> findAll() {
        return new ArrayList<>(vendors.values());
    }
    
    public boolean exists(String id) {
        return vendors.containsKey(id);
    }
    
    public void deleteAll() {
        vendors.clear();
    }
    
    public int size() {
        return vendors.size();
    }
}