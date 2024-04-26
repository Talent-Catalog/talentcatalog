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

package org.tctalent.server.configuration;

import static org.springframework.messaging.simp.SimpMessageType.CONNECT;
import static org.springframework.messaging.simp.SimpMessageType.DISCONNECT;
import static org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

/**
 * Websocket security set up.
 * <a href="https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html#page-title">
 *   Instructions for Spring 3 websocket config. </a>
 * @author John Cameron
 * @author Andrew Todd
 */
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
        MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        messages.simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT).permitAll()
            .simpDestMatchers("/app/**").permitAll()
            .simpSubscribeDestMatchers("/topic/**").permitAll()
            .anyMessage().denyAll();

        return messages.build();
    }
}