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

package org.tctalent.server.repository.db;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

//    @Override
//    @NotNull
//    @CacheEvict(value = "userCache", keyGenerator = "userKeyGenerator")
//    User save(@NotNull User user);

    @Query("select distinct u from User u "
            + " where lower(u.username) = lower(:username) "
            + " and u.role = :role "
            + " and u.status != 'deleted' ")
    User findByUsernameAndRole(@Param("username") String username,
                               @Param("role") Role role);

    @Query("select distinct u from User u "
            + " where lower(u.username) = lower(:username) "
            + " and u.status != 'deleted'")
//    @Cacheable(value = "userCache", keyGenerator = "usernameKeyGenerator")
    User findByUsernameIgnoreCase(@Param("username") String username);

    /* Used for candidate authentication */
    @Query("select distinct u from User u "
            + " where (lower(u.email) = lower(:email) )"
            + " and u.status != 'deleted'")
    User findByEmailIgnoreCase(@Param("email") String email);

    @Query("select u from User u where u.resetToken = :token and u.status != 'deleted'")
    User findByResetToken(@Param("token") String token);

    @Query(" select distinct u from User u "
            + " where lower(concat(u.firstName, ' ', u.lastName)) like lower(:usersName)"
            + " and u.role != 'user'")
    Page<User> searchAdminUsersName(@Param("usersName") String usersName, Pageable pageable);

    @Query("select u from User u "
        + "where u.usingMfa = false "
        + "and u.role != 'user' "
        + "and u.status != 'deleted'")
    List<User> searchStaffNotUsingMfa();
}
