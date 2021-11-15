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
import org.junit.jupiter.api.Test;
import org.tbbtalent.server.util.locale.LocaleHelper.CodeTranslation;

class LocaleHelperTest {

    @Test
    void getCountryNameTranslations() {
        List<CodeTranslation> cts;

        cts = LocaleHelper.getCountryNameTranslations(null);
        assertNotNull(cts);
        assertTrue(cts.isEmpty());

        cts = LocaleHelper.getCountryNameTranslations("fa");
        assertNotNull(cts);
        assertFalse(cts.isEmpty());
    }

    @Test
    void getLanguageNameTranslations() {
        List<CodeTranslation> cts;

        cts = LocaleHelper.getCountryNameTranslations(null);
        assertNotNull(cts);
        assertTrue(cts.isEmpty());

        cts = LocaleHelper.getLanguageNameTranslations("fa");
        assertNotNull(cts);
        assertFalse(cts.isEmpty());
    }

    @Test
    void isKnownLanguageCode() {
        boolean known;

        known = LocaleHelper.isKnownLanguageCode(null);
        assertFalse(known);

        known = LocaleHelper.isKnownLanguageCode("??");
        assertFalse(known);

        known = LocaleHelper.isKnownLanguageCode("ar");
        assertTrue(known);
    }

    @Test
    void isRtlLanguage() {
        boolean rtl;

        rtl = LocaleHelper.isRtlLanguage("??");
        assertFalse(rtl);

        rtl = LocaleHelper.isRtlLanguage("fr");
        assertFalse(rtl);

        rtl = LocaleHelper.isRtlLanguage("ar");
        assertTrue(rtl);
    }
}
