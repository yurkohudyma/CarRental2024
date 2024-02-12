package com.hudyma.CarRental2024.controller.restcontroller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mgr")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerRestController {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('manager:read', 'admin:read')")
    public String get() {
        return "GET:: mgr controller";
    }
    @PostMapping
    @PreAuthorize("hasAuthority('manager:create')")
    @Hidden
    public String post() {
        return "POST:: mgr controller";
    }
    @PutMapping
    @PreAuthorize("hasAuthority('manager:update')")
    @Hidden
    public String put() {
        return "PUT:: mgr controller";
    }
    @DeleteMapping
    @PreAuthorize("hasAuthority('manager:delete')")
    @Hidden
    public String delete() {
        return "DELETE:: mgr controller";
    }
}
