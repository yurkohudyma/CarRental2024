package com.hudyma.CarRental2024.controller.restcontroller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/user")
@RequiredArgsConstructor
public class IndexRestController {

    private final UserDetails userDetails;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public String index (){
        log.info("...access granted for {}", userDetails.getUsername());
        return "index";
    }
}
