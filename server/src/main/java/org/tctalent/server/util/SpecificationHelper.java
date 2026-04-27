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

package org.tctalent.server.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUser;
import org.tctalent.server.model.db.User;

/**
 * Some helper methods for Specifications
 *
 * @author John Cameron
 */
public class SpecificationHelper {

    /**
     * Common code for checking whether the given user has unread messages for an opp's chats.
     * @param user This is the user who is reading chats
     * @param query Standard specification query
     * @param builder Standard specification builder
     * @param numberOfChatsToRead Query on JobChats which will be used to check for unread chats
     * @param numberOfChatsToReadRoot Root associated with query
     * @param chatsFilter Predicate which identifies which chats are being checked
     * @return Predicate indicating whether any unread chats were found
     */
    public static Predicate hasUnreadChats(User user, CriteriaQuery<?> query,
        CriteriaBuilder builder,
        Subquery<Long> numberOfChatsToRead, Root<JobChat> numberOfChatsToReadRoot,
        Predicate chatsFilter) {
    /*
      Look for any associated chats that are not fully read.

      - Chats where user has not read to the last post
      - Chats which have posts, but have never been read.
     */

        //CHATS WHICH ARE NOT READ TO END
        //ie last read post < last post in chat
        //Construct this predicate
        final Predicate notReadToEnd;

        /*
        Query returning the id of the last post in the chat that the user has read.
            select last_read_post.id from job_chat_user
            where job_chat_id = job_chat.id and user_id = :loggedInUser
         */
        Subquery<Long> lastReadPost = query.subquery(Long.class);
        Root<JobChatUser> lastReadPostRoot = lastReadPost.from(JobChatUser.class);

        //Create where clause from predicates
        Predicate equalsChat = builder.equal(lastReadPostRoot.get("chat").get("id"),
            numberOfChatsToReadRoot.get("id"));
        Predicate equalsUser = builder.equal(lastReadPostRoot.get("user").get("id"), user.getId());
        lastReadPost.select(lastReadPostRoot.get("lastReadPost").get("id")).where(
            builder.and(equalsChat, equalsUser)
        );

        /*
        Subquery returning the id of the last post in the chat - it will have the largest id.
            select max(id) from chat_post where job_chat_id = job_chat.id
         */
        Subquery<Long> lastPostId = query.subquery(Long.class);
        Root<ChatPost> lastPostIdRoot = lastPostId.from(ChatPost.class);
        Predicate equalsChat2 = builder.equal(
            lastPostIdRoot.get("jobChat").get("id"), numberOfChatsToReadRoot.get("id"));
        lastPostId.select(builder.max(lastPostIdRoot.get("id"))).where(equalsChat2);

        //For chats which are not fully read by the user, the user's last read post will
        //be less that the last post.
        notReadToEnd = builder.lessThan(lastReadPost, lastPostId);

        //NON-EMPTY CHATS WHICH HAVE NEVER BEEN READ
        //ie last read post < last post in chat
        //Construct this predicate
        final Predicate neverRead;

        Subquery<Long> numberOfPosts = query.subquery(Long.class);
        Root<ChatPost> numberOfPostsRoot = numberOfPosts.from(ChatPost.class);
        Predicate equalsChat3 = builder.equal(
            numberOfPostsRoot.get("jobChat").get("id"), numberOfChatsToReadRoot.get("id"));
        numberOfPosts.select(builder.count(numberOfPostsRoot)).where(equalsChat3);
        final Predicate chatHasPosts = builder.greaterThan(numberOfPosts, 0L);
        final Predicate unreadChat = builder.isNull(lastReadPost);
        neverRead = builder.and(unreadChat, chatHasPosts);

        //FINALLY...
        //Now combine the predicates and do the query checking if the number of not fully
        //read chats is greater than 0.
        final Predicate notFullyRead = builder.or(notReadToEnd, neverRead);
        numberOfChatsToRead.select(builder.count(numberOfChatsToReadRoot)).where(
            builder.and(chatsFilter, notFullyRead));
        final Predicate oppHasUnreadChats = builder.greaterThan(numberOfChatsToRead, 0L);
        return oppHasUnreadChats;
    }
}
