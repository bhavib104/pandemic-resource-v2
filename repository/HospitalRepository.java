// repository/HospitalRepository.java
package repository;

import model.Hospital;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HospitalRepository {
    private final Map<String, Hospital> hospitals = new ConcurrentHashMap<>();
    
    public void save(Hospital hospital) {
        hospitals.put(hospital.getId(), hospital);
    }
    
    public Optional<Hospital> findById(String id) {
        return Optional.ofNullable(hospitals.get(id));
    }
    
    public List<Hospital> findAll() {
        return new ArrayList<>(hospitals.values());
    }
    
    public boolean exists(String id) {
        return hospitals.containsKey(id);
    }
    
    public void deleteAll() {
        hospitals.clear();
    }
    
    public int size() {
        return hospitals.size();
    }
}