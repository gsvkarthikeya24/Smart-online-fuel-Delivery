package com.smartfuel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Configuration
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_CUSTOMER")) {
            response.sendRedirect("/customer/dashboard");
        } else if (roles.contains("ROLE_PROVIDER")) {
            response.sendRedirect("/provider/dashboard");
        } else if (roles.contains("ROLE_AGENT")) {
            response.sendRedirect("/agent/dashboard");
        } else {
            response.sendRedirect("/login?error=true");
        }
    }
}
