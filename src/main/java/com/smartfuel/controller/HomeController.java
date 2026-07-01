package com.smartfuel.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Set;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (roles.contains("ROLE_CUSTOMER")) {
            return "redirect:/customer/dashboard";
        } else if (roles.contains("ROLE_PROVIDER")) {
            return "redirect:/provider/dashboard";
        } else if (roles.contains("ROLE_AGENT")) {
            return "redirect:/agent/dashboard";
        }
        
        return "redirect:/login";
    }
}
