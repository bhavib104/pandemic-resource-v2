package repository;

import model.Admin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdminRepository {
    private final Map<String, Admin> admins = new ConcurrentHashMap<>();
    
    public void save(Admin admin) {
        admins.put(admin.getId(), admin);
    }
    
    public Optional<Admin> findById(String id) {
        return Optional.ofNullable(admins.get(id));
    }
    
    public Optional<Admin> findByUsername(String username) {
        return admins.values().stream()
            .filter(admin -> admin.getUsername().equals(username))
            .findFirst();
    }
    
    public List<Admin> findAll() {
        return new ArrayList<>(admins.values());
    }
    
    public void deleteAll() {
        admins.clear();
    }
    
    public boolean exists(String id) {
        return admins.containsKey(id);
    }
    
    public int size() {
        return admins.size();
    }
}