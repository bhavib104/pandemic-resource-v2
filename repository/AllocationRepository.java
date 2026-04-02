// repository/AllocationRepository.java
package repository;

import model.Allocation;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AllocationRepository {
    private final List<Allocation> allocations = new CopyOnWriteArrayList<>();
    
    public void save(Allocation allocation) {
        allocations.add(allocation);
    }
    
    public void saveAll(List<Allocation> allocations) {
        this.allocations.addAll(allocations);
    }
    
    public List<Allocation> findAll() {
        return new ArrayList<>(allocations);
    }
    
    public void deleteAll() {
        allocations.clear();
    }
}