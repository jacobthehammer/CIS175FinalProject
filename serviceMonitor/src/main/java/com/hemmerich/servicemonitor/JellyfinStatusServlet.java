package com.hemmerich.servicemonitor;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "JellyfinStatusServlet", urlPatterns = {"/status/jellyfin"})
public class JellyfinStatusServlet extends HttpServlet {

    private volatile boolean isOnline = true; // Start online
    private ScheduledExecutorService executor;
    private final Random random = new Random();
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        executor = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            // Randomly determine the next state
            isOnline = random.nextBoolean();
        };

        // Start the task after 1 MINUTE, then repeat every 1 MINUTE
        executor.scheduleAtFixedRate(task, 1, 1, TimeUnit.MINUTES);
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
        executor.shutdown();
    }
}