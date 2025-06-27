/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.data;

import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppExtended;

import java.util.List;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUserInfo;

public class JobChatTestData {

    public static JobChat getChat() {
        JobChat chat = new JobChat();
        chat.setId(99L);
        chat.setJobOpp(getSalesforceJobOppExtended());
        return chat;
    }

    public static List<JobChat> getListOfChats() {
        JobChat chat1 = getChat();
        JobChat chat2 = getChat();
        chat2.setId(100L);
        JobChat chat3 = getChat();
        chat2.setId(101L);
        return List.of(
            chat1, chat2, chat3
        );
    }

    public static ChatPost getChatPost() {
        ChatPost post = new ChatPost();
        post.setId(199L);
        post.setContent("Post 1");
        return post;
    }

    public static List<ChatPost> getListOfPosts() {
        ChatPost post1 = getChatPost();
        ChatPost post2 = getChatPost();
        post2.setContent("Post 2");
        ChatPost post3 = getChatPost();
        post2.setContent("Post 3");
        return List.of(
            post1, post2, post3
        );
    }

    public static JobChatUserInfo getJobChatUserInfo() {
        JobChatUserInfo info = new JobChatUserInfo();
        info.setLastPostId(123L);
        info.setLastReadPostId(100L);
        return info;
    }
}
