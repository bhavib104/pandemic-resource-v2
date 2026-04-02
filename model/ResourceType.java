// model/ResourceType.java
package model;

import java.util.List;

public enum ResourceType {
    VENTILATOR("ventilator", "Critical respiratory support equipment"),
    MASK("mask", "Protective face masks"),
    VACCINE("vaccine", "COVID-19 vaccines"),
    PPE("ppe", "Personal Protective Equipment"),
    BED("bed", "Hospital beds"),
    OXYGEN("oxygen", "Medical oxygen cylinders"),
    TEST_KIT("test_kit", "COVID-19 testing kits"),
    MEDICINE("medicine", "Antiviral medications");
    
    private final String code;
    private final String description;
    
    ResourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ResourceType fromCode(String code) {
        for (ResourceType type : ResourceType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
    
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
    
    public static List<String> getAllCodes() {
        List<String> codes = new java.util.ArrayList<>();
        for (ResourceType type : ResourceType.values()) {
            codes.add(type.code);
        }
        return codes;
    }
}