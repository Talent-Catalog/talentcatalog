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

package org.tctalent.server.api.chat;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.JobChatUserService;
import org.tctalent.server.service.db.UserService;

/**
 * This is where websocket connections are handled.
 * <p/>
 * Websocket urls with the /app prefix are directed here.
 * This is configured in {@link org.tctalent.server.configuration.WebSocketConfig}.
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
    private final ChatPostService chatPostService;
    private final JobChatService jobChatService;
    private final UserService userService;
    private final JobChatUserService jobChatUserService;

    /**
     * Receives a post on the given chat from the currently logged in user,
     * stores it on the database and multicasts the enriched version of the post (containing
     * created timestamp, posting user etc) to clients who are subscribed to that chat.
     * @param post User's post
     * @param chatId ID of chat where the post has been made
     * @return Recorded post which is sent to all current subscribers of the chat with that id
     */
    @MessageMapping("/chat/{chatId}")
    @SendTo(ChatPostService.CHAT_PUBLISH_ROOT + "/{chatId}")
    public Map<String, Object> sendPost(Post post, @DestinationVariable("chatId") Long chatId) {
        final User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new UnauthorisedActionException("publish post");
        }

        JobChat chat = jobChatService.getJobChat(chatId);

        //Remember whether the chat was read by the user prior to this post.
        boolean wasReadByUser = jobChatUserService.isChatReadByUser(chat, loggedInUser);

        //Now create and add the post to the chat.
        ChatPost chatPost = chatPostService.createPost(post, chat, loggedInUser);

        if (wasReadByUser) {
            //We do special processing of posts for the user that originated them.
            //A user's own posts do not change their read state of the chat.
            //If the chat was read by the user prior to their post, update the JobChatUser info
            //so that the chat is still marked as read - taking into account the new post.
            //This just means setting the last read post in the JobChatUser info to the new post.
            //That is what markChatAsRead does.
            jobChatUserService.markChatAsRead(chat, loggedInUser, chatPost);
        }

        return chatPostService.getChatPostDtoBuilder().build(chatPost);
    }

}
