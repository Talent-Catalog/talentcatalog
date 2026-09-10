/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.User;

class SpecificationHelperTest {

  @Test
  void hasUnreadChatsBuildsAndReturnsUnreadChatPredicate() {
    User user = mock(User.class);
    when(user.getId()).thenReturn(42L);

    CriteriaQuery query = mock(
        CriteriaQuery.class,
        RETURNS_DEEP_STUBS
    );

    CriteriaBuilder builder = mock(
        CriteriaBuilder.class,
        RETURNS_DEEP_STUBS
    );

    Subquery numberOfChatsToRead = mock(
        Subquery.class,
        RETURNS_DEEP_STUBS
    );

    Root numberOfChatsToReadRoot = mock(
        Root.class,
        RETURNS_DEEP_STUBS
    );

    Predicate chatsFilter = mock(Predicate.class);
    Predicate expectedResult = mock(Predicate.class);

    when(builder.greaterThan(numberOfChatsToRead, 0L))
        .thenReturn(expectedResult);

    Predicate result = SpecificationHelper.hasUnreadChats(
        user,
        query,
        builder,
        numberOfChatsToRead,
        numberOfChatsToReadRoot,
        chatsFilter
    );

    assertSame(expectedResult, result);

    verify(query, times(3)).subquery(Long.class);

    verify(user).getId();

    verify(numberOfChatsToReadRoot, atLeastOnce())
        .get("id");

    verify(builder).greaterThan(
        numberOfChatsToRead,
        0L
    );
  }
}