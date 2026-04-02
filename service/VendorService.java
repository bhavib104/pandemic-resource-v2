// service/VendorService.java
package service;

import model.Vendor;
import repository.VendorRepository;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VendorService {
    private final VendorRepository vendorRepository;
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    
    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }
    
    public Vendor createVendor(String name, double latitude, double longitude) {
        String id = String.valueOf(idGenerator.getAndIncrement());
        Vendor vendor = new Vendor(id, name, latitude, longitude);
        vendorRepository.save(vendor);
        return vendor;
    }
    
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
    
    public Optional<Vendor> getVendor(String id) {
        return vendorRepository.findById(id);
    }
    
    public boolean updateInventory(String id, Map<String, Integer> inventory) {
        Optional<Vendor> vendorOpt = vendorRepository.findById(id);
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            vendor.getInventory().clear();
            vendor.getInventory().putAll(inventory);
            return true;
        }
        return false;
    }
    
    public void reset() {
        vendorRepository.deleteAll();
        idGenerator.set(1);
    }
}