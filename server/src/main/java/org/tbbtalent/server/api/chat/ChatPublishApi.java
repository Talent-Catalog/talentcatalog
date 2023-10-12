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

package org.tbbtalent.server.api.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.tbbtalent.server.model.db.ChatPost;
import org.tbbtalent.server.model.db.chat.Post;
import org.tbbtalent.server.service.db.JobChatService;

/**
 * This is where websocket connections are handled.
 * <p/>
 * Websocket urls with the /app prefix are directed here.
 * This is configured in {@link org.tbbtalent.server.configuration.WebSocketConfig}.
 * <p/>
 * So, for example, {@link #sendPost} below would be executed in response to the url ending in
 * /app/chat/{chatId}.
 * <p/>
 * This code is modelled on
 * <a href="https://spring.io/guides/gs/messaging-stomp-websocket/">this example from Spring.io</a>
 * and <a href="https://stackoverflow.com/questions/27047310/path-variables-in-spring-websockets-sendto-mapping/27055764#27055764">
 *     this Stackoverflow post</a>
 *
 * @author John Cameron
 */
@Controller
@RequiredArgsConstructor
public class ChatPublishApi {
    private final JobChatService jobChatService;

    /**
     * Receives a post on the given chat from the currently logged in user,
     * stores it on the database and multicasts the enriched version of the post (containing
     * created timestamp, posting user etc) to clients who are subscribed to that chat.
     * @param post User's post
     * @param chatId ID of chat where the post has been made
     * @return Recorded post which is sent to all current subscribers of the chat with that id
     */
    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public Post sendPost(Post post, @DestinationVariable String chatId) {
        //TODO create post on database, then return updated version of it containing created date,
        //user etc
        ChatPost chatPost = jobChatService.createPost(post, chatId);

        //TODO JC Convert to use normal builders
        return post;
    }
}
