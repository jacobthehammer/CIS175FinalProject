package com.hemmerich.servicemonitor;

import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SettingsServlet", urlPatterns = {"/api/settings"})
public class SettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        boolean enabled = false;

        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = 'notifications_enabled'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                enabled = Boolean.parseBoolean(rs.getString("setting_value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Boolean> json = new HashMap<>();
        json.put("notifications", enabled);
        
        response.getWriter().print(new Gson().toJson(json));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Checkbox sends "on" if checked, null if unchecked
        String param = request.getParameter("notifications");
        String valueToStore = (param != null) ? "true" : "false";

        String sql = "UPDATE app_settings SET setting_value = ? WHERE setting_key = 'notifications_enabled'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, valueToStore);
            stmt.executeUpdate();
            System.out.println("Updated notifications to: " + valueToStore);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Redirect back to dashboard
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}