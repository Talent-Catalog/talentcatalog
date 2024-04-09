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


import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

/**
 * Custom key generator for caching based on lowercase usernames. It is designed for
 * repository methods that perform database lookups using a username parameter, such as
 * UserRepository.findByUsernameIgnoreCase.
 * <p>
 * If a null username is provided, the generator interrupts the caching process by
 * throwing an IllegalArgumentException, highlighting an improper use or configuration, and should
 * never happen as a username is required for the associated database operations.
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
      throw new IllegalArgumentException("Cache key generation failed: username is null.");
    }
    return params[0].toString().toLowerCase();
  }

}
