/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.service.policy;

import org.springframework.stereotype.Component;
import org.tctalent.server.security.TcUserDetails;

/**
 * Policy for access to TC chats.
 * <p>
 *     Driven by user permissions such as "CHAT_SUBSCRIBE" specified as "authorities" in
 *     the TC security principal - which is a TcUserDetails object.
 * </p>
 *
 * @author John Cameron
 */
@Component
public class ChatPolicy {

    public boolean canSubscribeToChats(TcUserDetails user) {
        return user.hasAnyAuthority("CHAT_SUBSCRIBE");
    }

    public boolean canCreateChats(TcUserDetails user) {
        return user.hasAnyAuthority("CHAT_CREATE");
    }
}
