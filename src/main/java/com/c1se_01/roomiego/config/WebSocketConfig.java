package com.c1se_01.roomiego.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final UserHandshakeHandler userHandshakeHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/chatroom", "/user"); // FE sẽ subscribe topic
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/socket")
                .setHandshakeHandler(userHandshakeHandler)
                .setAllowedOrigins("http://localhost:5173", "https://cap2-fe.vercel.app")
                .withSockJS();
        registry.addEndpoint("/ws")
                .setHandshakeHandler(userHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setSendTimeLimit(60 * 1000)
                .setSendBufferSizeLimit(50 * 1024 * 1024)
                .setMessageSizeLimit(50 * 1024 * 1024);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(
                    Message<?> message,
                    MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    var usernames = accessor.getNativeHeader("username");
                    if (usernames != null && !usernames.isEmpty()) {
                        Principal principal = () -> usernames.get(0);
                        accessor.setUser(principal);
                    }
                }
                return message;
            }
        });
    }
}
