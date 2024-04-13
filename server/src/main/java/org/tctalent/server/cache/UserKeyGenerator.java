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

package org.tctalent.server.cache;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.User;

/**
 * Custom key generator for caching based on User. Used by repository methods such as
 * UserRepository.save.
 * <p>
 * If user is null, the generator interrupts the caching process, highlighting an improper use.
 * This should never happen as user is a required parameter for the associated database operations.
 *
 * @author sadatmalik
 */
@Component("userKeyGenerator")
@Slf4j
public class UserKeyGenerator implements KeyGenerator {

  @NotNull
  @Override
  public Object generate(@NotNull Object target, @NotNull Method method, Object... params) {
    if (params[0] == null) {
      throw new IllegalArgumentException("Cache key generation failed: user is null.");
    }
    if (!(params[0] instanceof User)) {
      throw new IllegalArgumentException("Cache key generation failed: user is not of type User.");
    }

    return ((User) params[0]).getUsername().toLowerCase();
  }

}
