// service/HospitalService.java (Complete)
package service;

import model.Hospital;
import repository.HospitalRepository;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HospitalService {
    private final HospitalRepository hospitalRepository;
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    
    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }
    
    public Hospital createHospital(String name, double latitude, double longitude, int urgencyLevel) {
        String id = String.valueOf(idGenerator.getAndIncrement());
        Hospital hospital = new Hospital(id, name, latitude, longitude, urgencyLevel);
        hospitalRepository.save(hospital);
        return hospital;
    }
    
    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }
    
    public Optional<Hospital> getHospital(String id) {
        return hospitalRepository.findById(id);
    }
    
    public boolean updateDemand(String id, Map<String, Integer> demand) {
        Optional<Hospital> hospitalOpt = hospitalRepository.findById(id);
        if (hospitalOpt.isPresent()) {
            Hospital hospital = hospitalOpt.get();
            hospital.getDemand().clear();
            hospital.getDemand().putAll(demand);
            return true;
        }
        return false;
    }
    
    public void reset() {
        hospitalRepository.deleteAll();
        idGenerator.set(1);
    }
}