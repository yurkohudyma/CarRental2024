package com.hudyma.CarRental2024.service;

import org.springframework.security.core.userdetails.UserDetails;

public class JwtService {
    public String extractUsername(String jwt) {
        return null;
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        return true;
    }
}
