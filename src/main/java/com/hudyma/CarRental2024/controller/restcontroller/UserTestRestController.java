package com.hudyma.CarRental2024.controller.restcontroller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserTestRestController {

    @GetMapping
    public String get() {
        return "GET:: user controller";
    }
    @PostMapping
    @Hidden
    public String post() {
        return "POST:: user controller";
    }
    @PutMapping
    @Hidden
    public String put() {
        return "PUT:: user controller";
    }
    @DeleteMapping
    @Hidden
    public String delete() {
        return "DELETE:: user controller";
    }
}
