// server/MainServer.java (Fixed with proper exception handling)
package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import controller.*;
import service.*;
import repository.*;
import engine.AllocationEngine;

import util.RequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MainServer {
    private final HttpServer server;
    
    // Repositories
    private final HospitalRepository hospitalRepository;
    private final VendorRepository vendorRepository;
    private final AllocationRepository allocationRepository;
    
    // Services
    private final HospitalService hospitalService;
    private final VendorService vendorService;
    private final AllocationService allocationService;
    private final CitizenService citizenService;
    
    // Controllers
    private final HospitalController hospitalController;
    private final VendorController vendorController;
    private final AllocationController allocationController;
    private final CitizenController citizenController;
    
    public MainServer() throws IOException {
        // Initialize repositories
        hospitalRepository = new HospitalRepository();
        vendorRepository = new VendorRepository();
        allocationRepository = new AllocationRepository();
        
        // Initialize services
        hospitalService = new HospitalService(hospitalRepository);
        vendorService = new VendorService(vendorRepository);
        allocationService = new AllocationService(allocationRepository, hospitalService, vendorService, new AllocationEngine());
        citizenService = new CitizenService(hospitalService, vendorService);
        
        // Initialize controllers
        hospitalController = new HospitalController(hospitalService);
        vendorController = new VendorController(vendorService);
        allocationController = new AllocationController(allocationService);
        citizenController = new CitizenController(citizenService);
        
        // Create server
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Setup routes
        setupRoutes();
        
        server.setExecutor(null);
    }
    
    private void setupRoutes() {
        // Hospital routes
        server.createContext("/hospitals", new RouteHandler() {
            @Override
            public String handleGet(Map<String, String> params, String body) {
                return hospitalController.handleGet();
            }
            
            @Override
            public String handlePost(Map<String, String> params, String body) {
                return hospitalController.handlePost(body);
            }
        });
        
        // Hospital with ID routes
        server.createContext("/hospitals/", new RouteHandler() {
            @Override
            public String handlePut(Map<String, String> params, String body) {
                if (params.containsKey("id") && params.get("action").equals("demand")) {
                    return hospitalController.handlePutDemand(params.get("id"), body);
                }
                return errorResponse("Invalid endpoint");
            }
        });
        
        // Vendor routes
        server.createContext("/vendors", new RouteHandler() {
            @Override
            public String handleGet(Map<String, String> params, String body) {
                return vendorController.handleGet();
            }
            
            @Override
            public String handlePost(Map<String, String> params, String body) {
                return vendorController.handlePost(body);
            }
        });
        
        // Vendor with ID routes
        server.createContext("/vendors/", new RouteHandler() {
            @Override
            public String handlePut(Map<String, String> params, String body) {
                if (params.containsKey("id") && params.get("action").equals("inventory")) {
                    return vendorController.handlePutInventory(params.get("id"), body);
                }
                return errorResponse("Invalid endpoint");
            }
        });
        
        // Allocation routes
        server.createContext("/allocate", new RouteHandler() {
            @Override
            public String handleGet(Map<String, String> params, String body) {
                return allocationController.handleGet();
            }
            
            @Override
            public String handlePost(Map<String, String> params, String body) {
                return allocationController.handlePost();
            }
        });
        
        // Citizen routes
        server.createContext("/nearest-hospitals", new RouteHandler() {
            @Override
            public String handleGet(Map<String, String> params, String body) {
                return citizenController.handleNearestHospitals(params);
            }
        });
        
        server.createContext("/resource-availability", new RouteHandler() {
            @Override
            public String handleGet(Map<String, String> params, String body) {
                return citizenController.handleResourceAvailability(params);
            }
        });
    }
    
    public void start() {
        server.start();
        System.out.println("Server started on port 8080");
        System.out.println("Available endpoints:");
        System.out.println("  POST /hospitals");
        System.out.println("  GET /hospitals");
        System.out.println("  PUT /hospitals/{id}/demand");
        System.out.println("  POST /vendors");
        System.out.println("  GET /vendors");
        System.out.println("  PUT /vendors/{id}/inventory");
        System.out.println("  POST /allocate");
        System.out.println("  GET /allocations");
        System.out.println("  GET /nearest-hospitals?lat=&lon=");
        System.out.println("  GET /resource-availability?lat=&lon=&type=");
    }
    
    public static void main(String[] args) throws IOException {
        MainServer mainServer = new MainServer();
        mainServer.start();
        System.out.println("\nServer is running. Press Ctrl+C to stop.");
    }
    
    private String errorResponse(String message) {
        return String.format("{\"error\":\"%s\"}", message);
    }
    
    private abstract class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            
            // Get body - now handles IOException properly
            String body = "";
            try {
                body = RequestParser.getBody(exchange);
            } catch (IOException e) {
                String errorResponse = errorResponse("Failed to read request body: " + e.getMessage());
                sendResponse(exchange, errorResponse, 400);
                return;
            }
            
            Map<String, String> params = RequestParser.getQueryParams(query);
            
            // Extract path parameters for nested routes
            if (path.matches("/hospitals/\\d+/demand")) {
                String[] parts = path.split("/");
                params.put("id", parts[2]);
                params.put("action", "demand");
            } else if (path.matches("/vendors/\\d+/inventory")) {
                String[] parts = path.split("/");
                params.put("id", parts[2]);
                params.put("action", "inventory");
            }
            
            String response;
            int statusCode = 200;
            
            try {
                switch (method) {
                    case "GET":
                        response = handleGet(params, body);
                        break;
                    case "POST":
                        response = handlePost(params, body);
                        break;
                    case "PUT":
                        response = handlePut(params, body);
                        break;
                    default:
                        response = errorResponse("Method not supported");
                        statusCode = 405;
                }
            } catch (Exception e) {
                response = errorResponse("Server error: " + e.getMessage());
                statusCode = 500;
                e.printStackTrace();
            }
            
            sendResponse(exchange, response, statusCode);
        }
        
        private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
        
        public String handleGet(Map<String, String> params, String body) {
            return errorResponse("GET not implemented for this endpoint");
        }
        
        public String handlePost(Map<String, String> params, String body) {
            return errorResponse("POST not implemented for this endpoint");
        }
        
        public String handlePut(Map<String, String> params, String body) {
            return errorResponse("PUT not implemented for this endpoint");
        }
    }
}