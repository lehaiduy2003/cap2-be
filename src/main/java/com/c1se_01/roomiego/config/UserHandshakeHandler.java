package com.c1se_01.roomiego.config;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    @NonNull
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            HttpServletRequest httpRequest = servletServerHttpRequest.getServletRequest();
            String username = httpRequest.getParameter("username");

            if (username != null && !username.isBlank()) {
                return new StompPrincipal(username);
            }
        }

        return new StompPrincipal(UUID.randomUUID().toString());
    }

    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
