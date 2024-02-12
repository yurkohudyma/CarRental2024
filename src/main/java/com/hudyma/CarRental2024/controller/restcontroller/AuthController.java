package com.hudyma.CarRental2024.controller.restcontroller;

import com.hudyma.CarRental2024.auth.AuthenticationRequest;
import com.hudyma.CarRental2024.auth.AuthenticationResponse;
import com.hudyma.CarRental2024.model.Role;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
@Log4j2
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping
    public String login(AuthenticationRequest req, Model model
                        /*@AuthenticationPrincipal UserDetails userDetails*/) {
        ResponseEntity<AuthenticationResponse> response =
                ResponseEntity.ok(authService.authenticate(req));
        var statusCode = response.getStatusCode();
        String email = req.getEmail();
        if (statusCode == HttpStatus.FORBIDDEN) {
            model.addAttribute("auth_error", true);
            log.error("...LOGIN::: {} access forbidden, 403", email);
            return "index";
        } else if (statusCode == HttpStatus.OK) {
            log.info("... LOGIN: {} access granted, 200 OK", email);
            //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Role role = userRepository.findRoleByEmail(email);
            switch (role) {
                case ADMIN -> {
                    log.info("... LOGIN: ADMIN access granted");
                    return "redirect:/orders";
                }
                case MANAGER -> {
                    log.info("... LOGIN: MGR access granted");
                    return "mgr";
                }
                case USER -> {
                    log.info("... LOGIN: User access granted");
                    return "user";
                }
                default -> {
                    model.addAttribute("unknown_role_error", true);
                    log.error("...LOGIN::: {} role auth error", email);
                }
            }
        }
        model.addAttribute("email", email);
        return "index";
    }
}
