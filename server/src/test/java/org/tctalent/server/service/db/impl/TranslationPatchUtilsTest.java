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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TranslationPatchUtilsTest {

    @Test
    void isValidPatchKey_acceptsExpectedFormat() {
        assertTrue(TranslationPatchUtils.isValidPatchKey("SERVICES.VERIFY_PLUS.TAG"));
        assertTrue(TranslationPatchUtils.isValidPatchKey("REGISTRATION.HEADER.TITLE.VERIFYPLUS"));
        assertTrue(TranslationPatchUtils.isValidPatchKey("REGISTRATION.HEADER.TITLE.CONTACT/ADDITIONAL"));
        assertFalse(TranslationPatchUtils.isValidPatchKey("services.verify_plus.tag"));
        assertFalse(TranslationPatchUtils.isValidPatchKey("SERVICES"));
        assertFalse(TranslationPatchUtils.isValidPatchKey("SERVICES..TAG"));
    }

    @Test
    void deepMerge_mergesOnlyPatchPaths() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("SERVICES", new LinkedHashMap<>(Map.of(
            "VERIFY_PLUS", new LinkedHashMap<>(Map.of(
                "TAG", "Verify+",
                "TITLE", "UNHCR Verify+"
            )),
            "UNHCR", new LinkedHashMap<>(Map.of("TITLE", "UNHCR Help"))
        )));

        Map<String, Object> patch = new LinkedHashMap<>();
        patch.put("SERVICES", new LinkedHashMap<>(Map.of(
            "VERIFY_PLUS", new LinkedHashMap<>(Map.of("TITLE", "UNHCR Verify+ Updated"))
        )));

        Map<String, Object> merged = TranslationPatchUtils.deepMerge(new LinkedHashMap<>(source), patch);

        assertEquals("Verify+",
            TranslationPatchUtils.getFlattenedValue(merged, "SERVICES.VERIFY_PLUS.TAG"));
        assertEquals("UNHCR Verify+ Updated",
            TranslationPatchUtils.getFlattenedValue(merged, "SERVICES.VERIFY_PLUS.TITLE"));
        assertEquals("UNHCR Help",
            TranslationPatchUtils.getFlattenedValue(merged, "SERVICES.UNHCR.TITLE"));
    }

    @Test
    void nestedMapFromFlatEntries_buildsNestedStructure() {
        Map<String, String> flat = Map.of(
            "SERVICES.VERIFY_PLUS.TAG", "Verify+",
            "SERVICES.VERIFY_PLUS.TITLE", "UNHCR Verify+"
        );

        Map<String, Object> nested = TranslationPatchUtils.nestedMapFromFlatEntries(flat);

        assertEquals("Verify+", TranslationPatchUtils.getFlattenedValue(nested,
            "SERVICES.VERIFY_PLUS.TAG"));
        assertEquals("UNHCR Verify+", TranslationPatchUtils.getFlattenedValue(nested,
            "SERVICES.VERIFY_PLUS.TITLE"));
    }

    @Test
    void collectFlattenedAtPrefix_collectsLeafStrings() {
        Map<String, Object> nested = new LinkedHashMap<>();
        TranslationPatchUtils.setNestedValue(nested, "SERVICES.VERIFY_PLUS.TAG", "Verify+");
        TranslationPatchUtils.setNestedValue(nested, "SERVICES.VERIFY_PLUS.TITLE", "UNHCR Verify+");
        TranslationPatchUtils.setNestedValue(nested, "SERVICES.UNHCR.TITLE", "UNHCR Help");

        Map<String, String> flattened = new LinkedHashMap<>();
        TranslationPatchUtils.collectFlattenedAtPrefix(nested, "SERVICES.VERIFY_PLUS", flattened);

        assertEquals(2, flattened.size());
        assertEquals("Verify+", flattened.get("SERVICES.VERIFY_PLUS.TAG"));
        assertEquals("UNHCR Verify+", flattened.get("SERVICES.VERIFY_PLUS.TITLE"));
    }

    @Test
    void getFlattenedValue_returnsNullForMissingPath() {
        Map<String, Object> nested = new LinkedHashMap<>();
        TranslationPatchUtils.setNestedValue(nested, "SERVICES.VERIFY_PLUS.TAG", "Verify+");

        assertNull(TranslationPatchUtils.getFlattenedValue(nested,
            "SERVICES.VERIFY_PLUS.MISSING"));
    }
}
