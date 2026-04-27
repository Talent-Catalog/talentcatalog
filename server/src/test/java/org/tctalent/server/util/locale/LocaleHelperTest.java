/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.util.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LocaleHelperTest {

    @Test
    void getCountryNameTranslations() {
        Map<String, String> cts;

        cts = LocaleHelper.getCountryNameTranslations(null);
        assertNotNull(cts);
        assertTrue(cts.isEmpty());

        cts = LocaleHelper.getCountryNameTranslations("en");
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getCountryNameTranslations("fa");
        assertNotNull(cts);
        assertFalse(cts.isEmpty());
    }

    @Test
    void getDayOfWeekTranslations() {
        Map<DayOfWeek, String> cts;
        TextStyle textStyle;


        textStyle = TextStyle.NARROW_STANDALONE;

        cts = LocaleHelper.getDayOfWeekTranslations(null, textStyle);
        assertNotNull(cts);
        assertTrue(cts.isEmpty());

        cts = LocaleHelper.getDayOfWeekTranslations("en", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getDayOfWeekTranslations("ps", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        //In the current Java version we are using (11), there is no Pashto narrow standalone
        //translation for days of week - it defaults to the English (eg "M" for Monday).
        //When this gets fixed - in future Java versions - this test will fail. That will be good
        //news signifying that we now have Pashto translations for this, and we can remove the test.
        assertEquals("M", cts.get(DayOfWeek.MONDAY),
            "Pashto is currently not expected to have translations for this textStyle of day of weeks");

        cts = LocaleHelper.getDayOfWeekTranslations("fr", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getDayOfWeekTranslations("es", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getDayOfWeekTranslations("ar", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getDayOfWeekTranslations("tr", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());
    }

    @Test
    void getMonthTranslations() {
        Map<Month, String> cts;
        TextStyle textStyle;


        textStyle = TextStyle.SHORT_STANDALONE;

        cts = LocaleHelper.getMonthTranslations(null, textStyle);
        assertNotNull(cts);
        assertTrue(cts.isEmpty());

        cts = LocaleHelper.getMonthTranslations("en", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getMonthTranslations("fr", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getMonthTranslations("ar", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

        cts = LocaleHelper.getMonthTranslations("tr", textStyle);
        assertNotNull(cts);
        assertFalse(cts.isEmpty());
    }

    @Test
    void getLanguageNameTranslations() {
        Map<String, String> cts;

        cts = LocaleHelper.getLanguageNameTranslations(null);
        assertNotNull(cts);
        assertTrue(cts.isEmpty());

        cts = LocaleHelper.getLanguageNameTranslations("en");
        assertNotNull(cts);
        assertFalse(cts.isEmpty());

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

    @Test
    void getOwnLanguageDisplayName() {
        String name;

        name = LocaleHelper.getOwnLanguageDisplayName(null);
        assertNull(name);

        name = LocaleHelper.getOwnLanguageDisplayName("en");
        assertNotNull(name);
        assertEquals("english", name.toLowerCase());

        name = LocaleHelper.getOwnLanguageDisplayName("fr");
        assertNotNull(name);
        assertEquals("français", name.toLowerCase());
    }

    @Test
    void getOffsetDateTimeTestFallbackToUTCOnBadTimezone() {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        final OffsetDateTime timeWithBadTimeZone = LocaleHelper.getOffsetDateTime(date, time,
            "fred");

        final OffsetDateTime timeWithUTCTimeZone = LocaleHelper.getOffsetDateTime(date, time,
            "UTC");

        assertEquals(timeWithUTCTimeZone, timeWithBadTimeZone);

        final OffsetDateTime timeWithOtherValidTimeZone = LocaleHelper.getOffsetDateTime(date, time,
            "Australia/Sydney");

        assertNotEquals(timeWithUTCTimeZone, timeWithOtherValidTimeZone);

    }
}
