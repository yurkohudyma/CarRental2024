package com.hudyma.CarRental2024.auth;

import com.hudyma.CarRental2024.constants.UserAccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private UserAccessLevel accessLevel;
}
