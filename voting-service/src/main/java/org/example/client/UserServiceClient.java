package org.example.client;

import org.example.config.FeignClientConfig;
import org.example.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "user-service",
        url = "http://user-service:8082",
        configuration = FeignClientConfig.class
)
public interface UserServiceClient {
    @GetMapping("/api/users/{username}")
    ResponseEntity<UserDTO> getUserByUsername(
            @RequestHeader("Authorization") String token,
            @PathVariable String username
    );

}