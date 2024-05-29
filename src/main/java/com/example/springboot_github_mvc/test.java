package com.example.springboot_github_mvc;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@SessionAttributes("user")
@Controller
@RequiredArgsConstructor
public class test {
    @GetMapping("login")
    public String viewLoginPage(HttpSession session, Model model, @RequestParam(required = false) String error) {
        try {
            return "UI/pages/loginPage";
        } catch (Exception e) {
            return "UI/pages/loginPage";
        }
    }
}
