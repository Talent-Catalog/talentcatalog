/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@NoArgsConstructor
final class TranslationPatchUtils {

    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[A-Z0-9_\\-/]+(\\.[A-Z0-9_\\-/]+)+$");

    static boolean isValidPatchKey(String key) {
        if (key == null) {
            return false;
        }
        return VALID_KEY_PATTERN.matcher(key).matches();
    }

    static Map<String, Object> deepMerge(Map<String, Object> target, Map<String, Object> patch) {
        for (Map.Entry<String, Object> entry : patch.entrySet()) {
            String key = entry.getKey();
            Object patchValue = entry.getValue();
            Object targetValue = target.get(key);

            if (patchValue instanceof Map<?, ?> patchMap && targetValue instanceof Map<?, ?> targetMap) {
                Map<String, Object> mergedChild = deepMerge(
                    new LinkedHashMap<>((Map<String, Object>) targetMap),
                    (Map<String, Object>) patchMap
                );
                target.put(key, mergedChild);
            } else {
                target.put(key, patchValue);
            }
        }
        return target;
    }

    static Map<String, Object> nestedMapFromFlatEntries(Map<String, String> flatEntries) {
        Map<String, Object> nested = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : flatEntries.entrySet()) {
            setNestedValue(nested, entry.getKey(), entry.getValue());
        }

        return nested;
    }

    static void setNestedValue(Map<String, Object> root, String dotPath, String value) {
        String[] keys = dotPath.split("\\.");
        Map<String, Object> node = root;
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i].toUpperCase(Locale.ROOT);
            Object nextNode = node.get(key);
            if (!(nextNode instanceof Map<?, ?>)) {
                nextNode = new LinkedHashMap<String, Object>();
                node.put(key, nextNode);
            }
            node = (Map<String, Object>) nextNode;
        }
        node.put(keys[keys.length - 1].toUpperCase(Locale.ROOT), value);
    }

    static void collectFlattened(
        Map<String, Object> source,
        String prefix,
        Map<String, String> flattened
    ) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String path = prefix == null || prefix.isBlank() ? key : prefix + "." + key;
            if (value instanceof Map<?, ?> child) {
                collectFlattened((Map<String, Object>) child, path, flattened);
            } else if (value instanceof String textValue) {
                flattened.put(path.toUpperCase(Locale.ROOT), textValue);
            }
        }
    }

    static void collectFlattenedAtPrefix(
        Map<String, Object> source,
        String prefix,
        Map<String, String> flattened
    ) {
        String normalizedPrefix = prefix.toUpperCase(Locale.ROOT);
        String[] keys = normalizedPrefix.split("\\.");
        Object node = source;
        for (String key : keys) {
            if (!(node instanceof Map<?, ?> currentMap)) {
                return;
            }
            node = ((Map<String, Object>) currentMap).get(key);
            if (node == null) {
                return;
            }
        }

        if (node instanceof Map<?, ?> childMap) {
            collectFlattened((Map<String, Object>) childMap, normalizedPrefix, flattened);
            return;
        }

        if (node instanceof String textValue) {
            flattened.put(normalizedPrefix, textValue);
        }
    }

    @Nullable
    static String getFlattenedValue(Map<String, Object> source, String keyPath) {
        String[] keys = keyPath.toUpperCase(Locale.ROOT).split("\\.");
        Object node = source;
        for (String key : keys) {
            if (!(node instanceof Map<?, ?> mapNode)) {
                return null;
            }
            node = ((Map<String, Object>) mapNode).get(key);
            if (node == null) {
                return null;
            }
        }
        return node instanceof String textValue ? textValue : null;
    }

    static List<String> sortedDistinctNonBlank(List<String> values) {
        if (values == null) {
            return List.of();
        }
        TreeSet<String> normalized = new TreeSet<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return new ArrayList<>(normalized);
    }

    static boolean hasAnyNonNullValues(Map<String, String> valuesByLanguage) {
        return valuesByLanguage != null && valuesByLanguage.values().stream().anyMatch(Objects::nonNull);
    }
}
