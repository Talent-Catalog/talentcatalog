/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.messaging.simp.SimpMessageType.CONNECT;
import static org.springframework.messaging.simp.SimpMessageType.DISCONNECT;
import static org.springframework.messaging.simp.SimpMessageType.UNSUBSCRIBE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@ExtendWith(MockitoExtension.class)
public class WebSocketSecurityConfigTest {
  @Mock
  private MessageMatcherDelegatingAuthorizationManager.Builder messagesBuilder;

  @Mock
  private MessageMatcherDelegatingAuthorizationManager.Builder.Constraint constraint;

  @InjectMocks
  private WebSocketSecurityConfig webSocketSecurityConfig;

  @Test
  void testAuthorizationManager() {
    when(messagesBuilder.simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT)).thenReturn(constraint);
    when(constraint.permitAll()).thenReturn(messagesBuilder);

    when(messagesBuilder.simpDestMatchers("/app/**")).thenReturn(constraint);
    when(constraint.authenticated()).thenReturn(messagesBuilder);

    when(messagesBuilder.simpSubscribeDestMatchers("/topic/**")).thenReturn(constraint);
    when(constraint.authenticated()).thenReturn(messagesBuilder);

    when(messagesBuilder.anyMessage()).thenReturn(constraint);
    when(constraint.authenticated()).thenReturn(messagesBuilder);

    when(messagesBuilder.build()).thenReturn(mock(AuthorizationManager.class));


    AuthorizationManager<Message<?>> authorizationManager = webSocketSecurityConfig.authorizationManager(messagesBuilder);

    assertNotNull(authorizationManager);
    assertNotNull(authorizationManager);
    verify(messagesBuilder).simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT);
    verify(constraint).permitAll();
    verify(messagesBuilder).simpDestMatchers("/app/**");
    verify(constraint, times(3)).authenticated();
    verify(messagesBuilder).simpSubscribeDestMatchers("/topic/**");
    verify(constraint, times(3)).authenticated();
    verify(messagesBuilder).anyMessage();
    verify(constraint, times(3)).authenticated();
    verify(messagesBuilder).build();
  }
}
