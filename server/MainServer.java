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
    
    private final HospitalRepository hospitalRepository;
    private final VendorRepository vendorRepository;
    private final AllocationRepository allocationRepository;
    
    private final HospitalService hospitalService;
    private final VendorService vendorService;
    private final AllocationService allocationService;
    private final CitizenService citizenService;
    
    private final HospitalController hospitalController;
    private final VendorController vendorController;
    private final AllocationController allocationController;
    private final CitizenController citizenController;
    
    public MainServer() throws IOException {
        hospitalRepository = new HospitalRepository();
        vendorRepository = new VendorRepository();
        allocationRepository = new AllocationRepository();
        
        hospitalService = new HospitalService(hospitalRepository);
        vendorService = new VendorService(vendorRepository);
        allocationService = new AllocationService(allocationRepository, hospitalService, vendorService, new AllocationEngine());
        citizenService = new CitizenService(hospitalService, vendorService);
        
        hospitalController = new HospitalController(hospitalService);
        vendorController = new VendorController(vendorService);
        allocationController = new AllocationController(allocationService);
        citizenController = new CitizenController(citizenService);
        
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        setupRoutes();
        server.setExecutor(null);
    }
    
    private void setupRoutes() {
        
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
        
        server.createContext("/hospitals/", new RouteHandler() {
            @Override
            public String handlePut(Map<String, String> params, String body) {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length >= 3) {
                    String id = parts[2];
                    return hospitalController.handlePutDemand(id, body);
                }
                return errorResponse("Invalid endpoint");
            }
        });
        
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
        
        server.createContext("/vendors/", new RouteHandler() {
            @Override
            public String handlePut(Map<String, String> params, String body) {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");
                if (parts.length >= 3) {
                    String id = parts[2];
                    return vendorController.handlePutInventory(id, body);
                }
                return errorResponse("Invalid endpoint");
            }
        });
        
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
    }
    
    public static void main(String[] args) throws IOException {
        MainServer server = new MainServer();
        server.start();
    }
    
    private String errorResponse(String message) {
        return String.format("{\"error\":\"%s\"}", message);
    }
    
    private abstract class RouteHandler implements HttpHandler {
        protected HttpExchange exchange;
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            this.exchange = exchange;
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            String body = RequestParser.getBody(exchange);
            
            Map<String, String> params = RequestParser.getQueryParams(query);
            
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
                response = errorResponse(e.getMessage());
                statusCode = 500;
                e.printStackTrace();
            }
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
        
        public String handleGet(Map<String, String> params, String body) {
            return errorResponse("GET not implemented");
        }
        
        public String handlePost(Map<String, String> params, String body) {
            return errorResponse("POST not implemented");
        }
        
        public String handlePut(Map<String, String> params, String body) {
            return errorResponse("PUT not implemented");
        }
    }
}