// util/RequestParser.java (Fixed)
package util;

import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestParser {
    
    public static Map<String, String> getQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }
    
    public static String getBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"))) {
            return reader.lines().collect(Collectors.joining());
        }
    }
    
    public static Map<String, String> extractPathParams(String path, String pattern) {
        Map<String, String> params = new HashMap<>();
        String[] pathParts = path.split("/");
        String[] patternParts = pattern.split("/");
        
        for (int i = 0; i < Math.min(pathParts.length, patternParts.length); i++) {
            if (patternParts[i].startsWith("{") && patternParts[i].endsWith("}")) {
                String paramName = patternParts[i].substring(1, patternParts[i].length() - 1);
                params.put(paramName, pathParts[i]);
            }
        }
        return params;
    }
}