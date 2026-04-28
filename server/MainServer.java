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
    private final AdminRepository adminRepository;

    // Services
    private final HospitalService hospitalService;
    private final VendorService vendorService;
    private final AllocationService allocationService;
    private final CitizenService citizenService;
    private final AdminService adminService;

    // Controllers
    private final HospitalController hospitalController;
    private final VendorController vendorController;
    private final AllocationController allocationController;
    private final CitizenController citizenController;
    private final AdminController adminController;

    // CORS configuration - Use specific origin instead of wildcard
    private static final String ALLOWED_ORIGIN = "http://localhost:3000"; // Your frontend URL
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String ALLOWED_HEADERS = "Content-Type, Authorization, X-Requested-With";
    private static final String ALLOW_CREDENTIALS = "true";

    public MainServer() throws IOException {
        // Initialize repositories
        hospitalRepository = new HospitalRepository();
        vendorRepository = new VendorRepository();
        allocationRepository = new AllocationRepository();
        adminRepository = new AdminRepository();

        // Initialize services
        hospitalService = new HospitalService(hospitalRepository);
        vendorService = new VendorService(vendorRepository);
        allocationService = new AllocationService(allocationRepository, hospitalService, vendorService,
                new AllocationEngine());
        citizenService = new CitizenService(hospitalService, vendorService);
        adminService = new AdminService(adminRepository, hospitalService, vendorService, allocationService);

        // Initialize controllers
        hospitalController = new HospitalController(hospitalService);
        vendorController = new VendorController(vendorService);
        allocationController = new AllocationController(allocationService);
        citizenController = new CitizenController(citizenService);
        adminController = new AdminController(adminService);

        // Create server
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        setupRoutes();
        server.setExecutor(null);
    }

    private void setupRoutes() {
        // ========== ADMIN ROUTES ==========
        server.createContext("/admin/login", new AdminRouteHandler() {
            @Override
            public String handle(String body, Map<String, String> params) {
                return adminController.handleLogin(body);
            }
        });

        server.createContext("/admin/logout", new AdminRouteHandler() {
            @Override
            public String handle(String body, Map<String, String> params) {
                return adminController.handleLogout();
            }
        });

        server.createContext("/admin/stats", new AdminRouteHandler() {
            @Override
            public String handle(String body, Map<String, String> params) {
                return adminController.handleGetStats();
            }
        });

        server.createContext("/admin/resources", new AdminRouteHandler() {
            @Override
            public String handle(String body, Map<String, String> params) {
                return adminController.handleGetResourceSummary();
            }
        });

        server.createContext("/admin/emergency", new AdminRouteHandler() {
            @Override
            public String handle(String body, Map<String, String> params) {
                return adminController.handleEmergencyReport();
            }
        });

        server.createContext("/admin/force-allocate", new AdminRouteHandler() {
            @Override
            public String handle(String body, Map<String, String> params) {
                return adminController.handleForceAllocation();
            }
        });

        server.createContext("/admin/create", new AdminRouteHandler() {
            @Override
            public String handle(String body, Map<String, String> params) {
                // System.out.println("Admin create endpoint hit with body: " + body);
                return adminController.handleCreateAdmin(body);
            }
        });

        // ========== HOSPITAL ROUTES ==========
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
                if (parts.length >= 4 && parts[3].equals("demand")) {
                    return hospitalController.handlePutDemand(parts[2], body);
                }
                return errorResponse("Invalid endpoint");
            }
        });

        // ========== VENDOR ROUTES ==========
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
                if (parts.length >= 4 && parts[3].equals("inventory")) {
                    return vendorController.handlePutInventory(parts[2], body);
                }
                return errorResponse("Invalid endpoint");
            }
        });

        // ========== ALLOCATION ROUTES ==========
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

        // ========== CITIZEN ROUTES ==========
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
        System.out.println("========================================");
        System.out.println("Server started on port 8080");
        System.out.println("CORS enabled for: " + ALLOWED_ORIGIN);
        System.out.println("========================================");
        System.out.println("Admin credentials:");
        System.out.println("  Username: admin");
        System.out.println("  Password: admin123");
        System.out.println("========================================");
        System.out.println("Admin Endpoints:");
        System.out.println("  POST /admin/login");
        System.out.println("  GET  /admin/stats");
        System.out.println("  GET  /admin/resources");
        System.out.println("  GET  /admin/emergency");
        System.out.println("  POST /admin/force-allocate");
        System.out.println("========================================");
    }

    public static void main(String[] args) throws IOException {
        MainServer server = new MainServer();
        server.start();
    }

    private String errorResponse(String message) {
        return String.format("{\"error\":\"%s\"}", message);
    }

    private void setCorsHeaders(HttpExchange exchange) {
        // Get the origin from the request
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        
        // Set specific origin instead of wildcard when credentials are involved
        if (origin != null && origin.equals(ALLOWED_ORIGIN)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        } else if (origin != null) {
            // For development, you can allow multiple origins
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        }
        
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", ALLOWED_METHODS);
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", ALLOW_CREDENTIALS);
        exchange.getResponseHeaders().set("Access-Control-Expose-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        // Set CORS headers before sending response
        setCorsHeaders(exchange);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        // Handle preflight OPTIONS request
        setCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1); // No content for preflight
        exchange.close();
    }

    // Admin route handler (simpler version)
    private abstract class AdminRouteHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle preflight OPTIONS request
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                handleOptionsRequest(exchange);
                return;
            }

            String method = exchange.getRequestMethod();
            String body = RequestParser.getBody(exchange);
            Map<String, String> params = RequestParser.getQueryParams(exchange.getRequestURI().getQuery());

            String response;
            int statusCode = 200;

            try {
                if (method.equals("POST") || method.equals("GET")) {
                    response = handle(body, params);
                } else {
                    response = errorResponse("Method not allowed");
                    statusCode = 405;
                }
            } catch (Exception e) {
                response = errorResponse(e.getMessage());
                statusCode = 500;
                e.printStackTrace();
            }

            sendResponse(exchange, response, statusCode);
        }

        public abstract String handle(String body, Map<String, String> params);
    }

    // Original route handler for other routes
    private abstract class RouteHandler implements HttpHandler {
        protected HttpExchange exchange;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle preflight OPTIONS request
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                handleOptionsRequest(exchange);
                return;
            }

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

            sendResponse(exchange, response, statusCode);
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