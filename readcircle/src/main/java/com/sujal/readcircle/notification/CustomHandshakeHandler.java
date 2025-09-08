package com.sujal.readcircle.notification;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Principal user = super.determineUser(request, wsHandler, attributes);
        if (user instanceof JwtAuthenticationToken jwtToken) {
            String email = jwtToken.getToken().getClaim("email");
            return () -> email; // return email as principal name
        }
        return user;
    }
}