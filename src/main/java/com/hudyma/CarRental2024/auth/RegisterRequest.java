package com.hudyma.CarRental2024.auth;

import com.hudyma.CarRental2024.constants.UserAccessLevel;
import com.hudyma.CarRental2024.model.Role;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
    private UserAccessLevel accessLevel;
}
