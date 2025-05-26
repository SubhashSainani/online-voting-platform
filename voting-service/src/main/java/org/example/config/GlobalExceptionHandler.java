package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception e, Model model) {
        String token = request.getParameter("token");
        model.addAttribute("error", e.getMessage());
        model.addAttribute("token", token);
        return "error";
    }
}