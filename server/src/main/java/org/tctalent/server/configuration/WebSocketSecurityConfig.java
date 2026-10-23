/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration;

import static org.springframework.messaging.simp.SimpMessageType.CONNECT;
import static org.springframework.messaging.simp.SimpMessageType.DISCONNECT;
import static org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Websocket security set up.
 * <p/>
 * Applies Spring Security to the STOMP messages received over our chat web socket - setting up
 * the security context from the user associated with each message and checking that the user is
 * authorized to send the message.
 * <p/>
 * We do not use {@code @EnableWebSocketSecurity} because it always requires a CSRF token in
 * STOMP CONNECT messages, and that cannot currently be turned off. Instead, the same Spring
 * Security components are configured here, without the CSRF check, as described
 * <a href="https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html#websocket-sameorigin-disable">
 * here</a>.
 * <p/>
 * We don't need the CSRF check because we don't use cookie (or session) based authentication,
 * which is what CSRF attacks exploit. Instead, the browser code adds the user's JWT token to the
 * STOMP CONNECT message (see {@link WebSocketConfig2}) - which a browser will never do
 * automatically for a request from another site. We have never had the CSRF check: this
 * replaces an AbstractSecurityWebSocketMessageBrokerConfigurer (removed in Spring Security 7)
 * with sameOriginDisabled() returning true.
 * <p/>
 * The order (same as the previous configuration and as {@code @EnableWebSocketSecurity}) must be
 * after {@link WebSocketConfig2} so that its JWT authentication interceptor has set the user on
 * a CONNECT message before these security interceptors see it.
 *
 * @author John Cameron
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final ApplicationContext applicationContext;

    /**
     * Allows {@code @AuthenticationPrincipal} to be used on {@code @MessageMapping} methods.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        AuthorizationChannelInterceptor authorizationInterceptor =
            new AuthorizationChannelInterceptor(inboundMessageAuthorizationManager());
        authorizationInterceptor.setAuthorizationEventPublisher(
            new SpringAuthorizationEventPublisher(applicationContext));

        registration.interceptors(new SecurityContextChannelInterceptor(), authorizationInterceptor);
    }

    /**
     * Authorization rules for messages received from clients.
     */
    private static AuthorizationManager<Message<?>> inboundMessageAuthorizationManager() {
        return MessageMatcherDelegatingAuthorizationManager.builder()
            .simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT).permitAll()
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/topic/**").authenticated()
            .anyMessage().authenticated()
            .build();
    }

}
