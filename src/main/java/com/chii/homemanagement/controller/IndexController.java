package com.chii.homemanagement.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@Tag(name = "index", description = "index")
@Slf4j
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexController {

    @GetMapping("/test")
    public String index(Model model, HttpSession session) {

        return "index";
    }

}
