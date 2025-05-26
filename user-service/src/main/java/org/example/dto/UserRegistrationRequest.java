package org.example.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String role;
}