package org.zeus.ims.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws ServletException, IOException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/dashboard";

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            switch (role) {
                case "OWNER":
                    redirectUrl = "/dashboard";
                    break;
                case "SALES":
                    redirectUrl = "/dashboard";
                    break;
                case "PRODUCTION_MANAGER":
                    redirectUrl = "/dashboard";
                    break;
                case "WORKSHOP_PERSONNEL":
                    redirectUrl = "/dashboard";
                    break;
                case "ACCOUNTANT":
                    redirectUrl = "/dashboard";
                    break;
                default:
                    redirectUrl = "/dashboard";
                    break;
            }
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
