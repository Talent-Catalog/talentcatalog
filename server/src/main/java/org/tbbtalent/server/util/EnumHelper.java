/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

/**
 * Enum utilities. 
 *
 * @author John Cameron
 */
public class EnumHelper {

    /**
     * Convert a list of enums into a commas separated string of enum names.
     * <p/>
     * Inverse of {@link #fromString}
     * @param vals List of enums
     * @param <T> Any enum type
     * @return Comma separated string
     */
    @Nullable
    public static <T extends Enum<T>> String toString(@Nullable List<T> vals) {
        return vals == null ? null : vals.stream().map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * Extracts a list of enums from a comma separated string of enum names
     * <p/>
     * Inverse of {@link #toString}
     * @param enumType Class of ehe type of the enum you are extracting into
     * @param csv comma separated string of enum names 
     * @param <T> Any enum type
     * @return List of enums (of type T)
     */
    @Nullable
    public static <T extends Enum<T>> List<T> fromString(
            Class<T> enumType, @Nullable String csv) {
        return csv == null || csv.trim().length() == 0 ? null : 
                Stream.of(csv.split(","))
                .map(s -> Enum.valueOf(enumType, s))
                .collect(Collectors.toList());
    }
    
}
