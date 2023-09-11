/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tbbtalent.server.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.TbbUserDetailsService;

/**
 * Additional WebSocket configuration.
 * <p/>
 * Used to support our chats.
 * <p/>
 * This is needed to allow token authentication in STOMP.
 *
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
    private TbbUserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
                StompHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String jwt = getAuthorizationToken(accessor);
                    if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                        String username = tokenProvider.getUsernameFromJwt(jwt);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                    }
                }
                return message;
            }
        });
    }

    private String getAuthorizationToken(MessageHeaderAccessor accessor) {
        final String header = (String) accessor.getHeader("Authorization");
        final String bearerLabel = "Bearer ";
        if (StringUtils.hasText(header) && header.startsWith(bearerLabel)) {
            return header.substring(bearerLabel.length());
        }
        return null;
    }


}
