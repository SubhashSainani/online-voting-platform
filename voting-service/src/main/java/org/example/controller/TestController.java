package org.example.controller;

import org.example.client.UserServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voting/test")
public class TestController {
    
    private final UserServiceClient userServiceClient;
    
    public TestController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }
    
    @GetMapping("/user-service")
    public ResponseEntity<String> testUserService() {
        try {
            return ResponseEntity.ok("Connection to user-service successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error connecting to user-service: " + e.getMessage());
        }
    }
}