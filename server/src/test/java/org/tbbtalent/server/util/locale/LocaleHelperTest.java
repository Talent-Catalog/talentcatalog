/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.util.locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbbtalent.server.util.locale.LocaleHelper.CodeTranslation;

class LocaleHelperTest {
    LocaleHelper helper;

    @BeforeEach
    void setUp() {
        helper = new LocaleHelper();
    }

    @AfterEach
    void tearDown() {
        helper = null;
    }

    @Test
    void getCountryNameTranslations() {
        List<CodeTranslation> cts = helper.getCountryNameTranslations("fa");
        assertNotNull(cts);
    }

    @Test
    void getLanguageNameTranslations() {
        List<CodeTranslation> cts = helper.getLanguageNameTranslations("fa");
        assertNotNull(cts);
    }

    @Test
    void isKnownLanguageCode() {
        boolean known;

        known = helper.isKnownLanguageCode("??");
        assertFalse(known);

        known = helper.isKnownLanguageCode("ar");
        assertTrue(known);
    }

    @Test
    void isRtlLanguage() {
        boolean rtl;

        rtl = helper.isRtlLanguage("??");
        assertFalse(rtl);

        rtl = helper.isRtlLanguage("fr");
        assertFalse(rtl);

        rtl = helper.isRtlLanguage("ar");
        assertTrue(rtl);
    }
}
