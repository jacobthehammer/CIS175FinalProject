package com.hemmerich.servicemonitor;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet(name = "AdGuardStatusServlet", urlPatterns = {"/status/adguard"})
public class AdGuardStatusServlet extends HttpServlet {

    private volatile boolean isOnline = true;
    private ScheduledExecutorService executor;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        executor = Executors.newSingleThreadScheduledExecutor();
        
        Runnable task = () -> {
            // 1. Flip Status
            isOnline = !isOnline; 
            
            // 2. Log to Database
            String sql = "INSERT INTO service_logs (service_name, status, event_time) VALUES (?, ?, NOW())";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, "AdGuard");
                stmt.setString(2, isOnline ? "online" : "offline");
                stmt.executeUpdate();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        
        // Run every 30 seconds
        executor.scheduleAtFixedRate(task, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", isOnline ? "online" : "offline");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(statusMap));
        }
    }

    @Override
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}