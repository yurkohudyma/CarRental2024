package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.auth.AuthenticationRequest;
import com.hudyma.CarRental2024.auth.AuthenticationResponse;
import com.hudyma.CarRental2024.model.Role;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@Log4j2
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping
    public String login(AuthenticationRequest authreq, Model model, HttpSession session) {
        ResponseEntity<AuthenticationResponse> response =
                ResponseEntity.ok(authService.authenticate(authreq));
        var statusCode = response.getStatusCode();
        String email = authreq.getEmail();
        if (statusCode == HttpStatus.FORBIDDEN) {
            model.addAttribute("auth_error", true);
            log.error("...LOGIN::: {} access forbidden, 403", email);
            getSessionContext(session);
            return "index";
        } else if (statusCode == HttpStatus.OK) {
            log.info("... LOGIN: {} access granted, 200 OK", email);
            Role role = userRepository.findRoleByEmail(email);
            switch (role) {
                case ADMIN -> {
                    log.info("... LOGIN: ADMIN access granted");
                    model.addAttribute("email", email);
                    model.addAttribute("isAdmin", true);
                    getSessionContext(session);

                    return "index";
                }
                case MANAGER -> {
                    log.info("... LOGIN: MGR access granted");
                    model.addAttribute("isManager", true);
                    getSessionContext(session);
                    return "redirect:/orders";
                }
                case USER -> {
                    log.info("... LOGIN: User access granted");
                    getSessionContext(session);
                    return "redirect:/demo";
                }
                default -> {
                    model.addAttribute("unknown_role_error", true);
                    log.error("...LOGIN::: {} role auth error", email);
                }
            }
        }
        model.addAttribute("email", email);
        getSessionContext(session);
        return "index";
    }

    public static void getSessionContext(HttpSession session) {
        log.info(session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
    }
}
