package com.hemmerich.servicemonitor;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// We use an annotation to map this servlet to the URL "/status/savannah"
@WebServlet(name = "SavannahStatusServlet", urlPatterns = {"/status/savannah"})
public class SavannahStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            // Always online
            out.print("{\"status\": \"online\"}");
        }
    }
}