package com.hudyma.CarRental2024.controller.restcontroller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mgr")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ManagerRestController {

    @GetMapping
    public String get() {
        return "GET:: mgr controller";
    }
    @PostMapping
    @Hidden
    public String post() {
        return "POST:: mgr controller";
    }
    @PutMapping
    @Hidden
    public String put() {
        return "PUT:: mgr controller";
    }
    @DeleteMapping
    @Hidden
    public String delete() {
        return "DELETE:: mgr controller";
    }
}
