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

import java.awt.ComponentOrientation;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;

/**
 * Utilities for accessing Locale related information
 *
 * @author John Cameron
 */
public class LocaleHelper {

    /**
     * Returns translations of all known countries in the given language
     * @param langCode Code of language - eg fr for French
     * @return Map of country code to translation - may be empty
     */
    public static @NotNull Map<String, String> getCountryNameTranslations(@Nullable String langCode) {
        Map<String, String> cts = new HashMap<>();
        if (langCode != null && isKnownLanguageCode(langCode)) {
            Locale translationLocale = new Locale(langCode);
            String[] countryCodes = Locale.getISOCountries();
            for (String countryCode : countryCodes) {
                Locale countryLocale = new Locale("", countryCode);
                String name = countryLocale.getDisplayCountry(translationLocale);
                cts.put(countryCode, name);
            }
        }
        return cts;
    }

    /**
     * Returns translations of days of the week (Sunday, Monday etc) in the given language.
     * Translations may be abbreviated as requested by the TextStyle parameter.
     * @param langCode Code of language - eg fr for French
     * @param textStyle Type of abbreviation, if any
     * @return Map of DayOfWeek to translation - may be empty
     */
    public static @NotNull Map<DayOfWeek, String> getDayOfWeekTranslations(
        @Nullable String langCode, @NotNull TextStyle textStyle) {
        Map<DayOfWeek, String> cts = new HashMap<>();
        if (langCode != null && isKnownLanguageCode(langCode)) {
            Locale translationLocale = new Locale(langCode);
            for (DayOfWeek dw : DayOfWeek.values()) {
                String name = dw.getDisplayName(textStyle, translationLocale);
                cts.put(dw, name);
            }
        }
        return cts;
    }

    /**
     * Returns translations of months (January, February etc) in the given language.
     * Translations may be abbreviated as requested by the TextStyle parameter.
     * @param langCode Code of language - eg fr for French
     * @param textStyle Type of abbreviation, if any
     * @return Map of Month to translation - may be empty
     */
    public static @NotNull Map<Month, String> getMonthTranslations(
        @Nullable String langCode, @NotNull TextStyle textStyle) {
        Map<Month, String> cts = new HashMap<>();
        if (langCode != null && isKnownLanguageCode(langCode)) {
            Locale translationLocale = new Locale(langCode);
            for (Month month : Month.values()) {
                String name = month.getDisplayName(textStyle, translationLocale);
                cts.put(month, name);
            }
        }
        return cts;
    }

    /**
     * Returns translations of all known languages in the given language
     * @param langCode Code of language - eg fr for French
     * @return Map of language code to translation - may be empty
     */
    public static @NotNull Map<String, String> getLanguageNameTranslations(@Nullable String langCode) {
        Map<String, String> cts = new HashMap<>();
        if (langCode != null && isKnownLanguageCode(langCode)) {
            Locale translationLocale = new Locale(langCode);
            String[] languageCodes = Locale.getISOLanguages();
            for (String languageCode : languageCodes) {
                Locale languageLocale = new Locale(languageCode);
                String name = languageLocale.getDisplayLanguage(translationLocale);
                cts.put(languageCode, name);
            }
        }
        return cts;
    }

    /**
     * Computes an OffsetDateTime from a LocalDate, LocalTime and timezone
     * @param localDate Local date
     * @param localTime Local time
     * @param timezone Timezone string - if null or blank UTC timezone is used
     * @return An OffsetDateTime
     */
    public static OffsetDateTime getOffsetDateTime(
        LocalDate localDate, LocalTime localTime, @Nullable String timezone) {
        ZoneOffset offset = ZoneOffset.UTC;
        if (!ObjectUtils.isEmpty(timezone)) {
            try {
                offset = ZoneId.of(timezone).getRules().getOffset(Instant.now());
            } catch (DateTimeException e) {
                System.err.println("Invalid timezone provided: " + timezone + ". Falling back to UTC.");
            }
        }
        return OffsetDateTime.of(localDate, localTime, offset);
    }

    /**
     * Returns the display name of a language in its own language.
     * <p/>
     * For example, "Francais" for the language "fr"
     * @param langCode Language code
     * @return Display name or null if langCode is null.
     */
    public static String getOwnLanguageDisplayName(@Nullable String langCode) {
        String name = null;
        if (langCode != null) {
            Locale languageLocale = new Locale(langCode);
            name = languageLocale.getDisplayLanguage(languageLocale);
        }
        return name;
    }

    /**
     * Checks whether the given language code is known.
     * @param langCode language code - eg 'fr' for French
     * @return True if code is known. False if null
     */
    public static boolean isKnownLanguageCode(@Nullable String langCode) {
        String[] languageCodes = Locale.getISOLanguages();
        return Arrays.asList(languageCodes).contains(langCode);
    }

    /**
     * Determines whether the given language is a right to left language - like Arabic
     * @param langCode language code - eg 'fr' for French
     * @return True if language is written right to left. False if null
     */
    public static boolean isRtlLanguage(@Nullable String langCode) {
        if (langCode == null) {
            return false;
        }
        ComponentOrientation o = ComponentOrientation.getOrientation(new Locale(langCode));
        return o == ComponentOrientation.RIGHT_TO_LEFT;
    }

}
