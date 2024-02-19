package com.hudyma.CarRental2024.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@RequestMapping("/admin")
@RequiredArgsConstructor
@Controller
public class AdminController {

    @GetMapping
    public String admin (){
        return "admin";
    }
}
