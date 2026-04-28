package model;

import java.util.*;

public class Admin {
    private String id;
    private String username;
    private String password;
    private String role;
    private String lastLogin;
    
    public Admin(String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.lastLogin = new Date().toString();
    }
    
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}