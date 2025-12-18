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

package org.tctalent.server.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * String utilities.
 *
 * Helper methods for converting between comma-delimited strings and lists.
 *
 * @author Ehsan Ehrari
 */
public class StringHelper {

  /**
   * Convert a list of Long values into a comma separated string.
   * <p/>
   * Inverse of {@link #getIdsFromString(String)}
   *
   * @param ids List of Long values
   * @return Comma separated string
   */
  @Nullable
  public static String getListAsString(@Nullable List<Long> ids) {
    return CollectionUtils.isEmpty(ids)
        ? null
        : ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
  }

  /**
   * Extracts a list of Long values from a comma separated string.
   * <p/>
   * Inverse of {@link #getListAsString(List)}
   *
   * @param csv Comma separated string
   * @return List of Long values
   */
  @Nullable
  public static List<Long> getIdsFromString(@Nullable String csv) {
    return !StringUtils.hasText(csv)
        ? null
        : Stream.of(csv.split(","))
            .map(String::trim)
            .map(Long::parseLong)
            .collect(Collectors.toList());
  }

  /**
   * Convert a list of Strings into a comma separated string.
   * <p/>
   * Inverse of {@link #getStringListFromString(String)}
   *
   * @param values List of strings
   * @return Comma separated string
   */
  @Nullable
  public static String getStringListAsString(@Nullable List<String> values) {
    return CollectionUtils.isEmpty(values)
        ? null
        : values.stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.joining(","));
  }

  /**
   * Extracts a list of Strings from a comma separated string.
   * <p/>
   * Inverse of {@link #getStringListAsString(List)}
   *
   * @param csv Comma separated string
   * @return List of strings
   */
  @Nullable
  public static List<String> getStringListFromString(@Nullable String csv) {
    return !StringUtils.hasText(csv)
        ? null
        : Stream.of(csv.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.toList());
  }
}
