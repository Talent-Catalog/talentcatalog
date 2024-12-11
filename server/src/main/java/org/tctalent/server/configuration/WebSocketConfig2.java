/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration;

import io.jsonwebtoken.JwtException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.tctalent.server.exception.ExpiredTokenException;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetailsService;

/**
 * Additional WebSocket configuration which checks authentication using the user's logged on
 * JWT token. In the browser code, the JWT token is added to the STOMP CONNECT message.
 * This code extracts the token and checks it.
 * <p/>
 * Used to support our chats.
 * <p/>
 * Based on guidance
 * <a href=
 * "https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication-token-based.html">
 * here</a>
 * @see WebSocketConfig
 * @author John Cameron
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig2 implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private TcUserDetailsService userDetailsService;

    /**
     * Intercept STOMP CONNECT message coming in over the web socket and extract and check the
     * JWT token.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
                StompHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String jwt = getAuthorizationToken(accessor);
                    if (StringUtils.hasText(jwt)) {
                        try {
                            if (tokenProvider.validateToken(jwt)) {
                                String username = tokenProvider.getUsernameFromJwt(jwt);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                accessor.setUser(authentication);
                            } else {
                                handleInvalidToken();
                            }
                        } catch (JwtException | IllegalArgumentException e) {
                            handleInvalidToken();
                        }
                    }
                }
                return message;
            }

            private void handleInvalidToken() {
                throw new ExpiredTokenException(JwtTokenProvider.EXPIRED_OR_INVALID_TOKEN_MSG);
            }

        });
    }

    /**
     * Authorization is a header on the STOMP CONNECT message (not an HTTP header).
     * Spring stores STOMP headers in the native headers.
     * @see StompHeaderAccessor
     * @param accessor Stomp header accessor
     * @return JWT authorization token, or null if none found
     */
    @Nullable
    private String getAuthorizationToken(StompHeaderAccessor accessor) {
        final String header = accessor.getFirstNativeHeader("Authorization");
        if (header != null) {
            final String bearerLabel = "Bearer ";
            if (StringUtils.hasText(header) && header.startsWith(bearerLabel)) {
                return header.substring(bearerLabel.length());
            }
        }
        return null;
    }
}
