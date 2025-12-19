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

package org.tctalent.server.service.db.impl;

import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUser;
import org.tctalent.server.model.db.JobChatUserInfo;
import org.tctalent.server.model.db.JobChatUserKey;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.JobChatUserRepository;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatUserService;


@AllArgsConstructor
@Service
public class JobChatUserServiceImpl implements JobChatUserService {
    private final JobChatUserRepository jobChatUserRepository;
    private final ChatPostService chatPostService;

    @NonNull
    @Override
    public JobChatUserInfo getJobChatUserInfo(@NonNull JobChat chat, @NonNull User user) {
        JobChatUserKey key = new JobChatUserKey(chat.getId(), user.getId());
        JobChatUser jcu = jobChatUserRepository.findById(key).orElse(null);

        JobChatUserInfo info = new JobChatUserInfo();

        ChatPost lastPost = chatPostService.getLastChatPost(chat.getId());
        Long lastPostId = lastPost == null ? null : lastPost.getId();
        info.setLastPostId(lastPostId);

        if (jcu != null) {
            ChatPost lastReadPost = jcu.getLastReadPost();
            info.setLastReadPostId(lastReadPost == null ? null : lastReadPost.getId());
        }

        return info;
    }

    @Override
    public boolean isChatReadByUser(@NonNull JobChat chat, @NonNull User user) {
        boolean isReadByUser;
        JobChatUserInfo info = getJobChatUserInfo(chat, user);
        if (info.getLastPostId() == null) {
            //No posts in chat. Consider as read;
            isReadByUser = true;
        } else if (info.getLastReadPostId() == null) {
            //User has not read post at all
            isReadByUser = false;
        } else {
            //Read if user has read up to last post
            isReadByUser = info.getLastReadPostId() >= info.getLastPostId();
        }
        return isReadByUser;
    }

    @Override
    public void markChatAsRead(@NonNull JobChat chat, @NonNull User user, @Nullable ChatPost post) {

        JobChatUserKey key = new JobChatUserKey(chat.getId(), user.getId());
        JobChatUser jcu = jobChatUserRepository.findById(key).orElse(null);

        if (jcu == null) {
            jcu = new JobChatUser();
            jcu.setId(key);
            jcu.setChat(chat);
            jcu.setUser(user);
            jcu.setLastReadPost(post);
        } else {
            jcu.setLastReadPost(post);
        }
        jobChatUserRepository.save(jcu);
    }
}
