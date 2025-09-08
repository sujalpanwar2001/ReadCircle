package com.sujal.readcircle.config;

import com.sujal.readcircle.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<String> {
//    @Override
//    public Optional<String> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication == null ||
//        !authentication.isAuthenticated() ||
//        authentication instanceof AnonymousAuthenticationToken){
//            return Optional.empty();
//        }
////        User userPrincipal = (User)authentication.getPrincipal();
//        return Optional.ofNullable(authentication.getName());
//
//
//    }

@Override
public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
        return Optional.empty();
    }

    // If principal is a JWT, extract username claim
    if (authentication.getPrincipal() instanceof Jwt jwt) {
        System.out.println("JWT Claims: " + jwt.getClaims());
        // Keycloak usually sets this claim
        String username =  jwt.getClaim("email");
        return Optional.ofNullable(username);
    }

    // fallback
    return Optional.ofNullable(authentication.getName());
}
}
