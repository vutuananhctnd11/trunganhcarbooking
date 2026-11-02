package com.example.trunganhcar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TripWebController {

    @GetMapping("/trip/{code}")
    public String tripTrackingPage(@PathVariable String code) {
        return "forward:/tracking.html";
    }
}

