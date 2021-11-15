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

import java.awt.ComponentOrientation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Utilities for accessing Locale related information
 *
 * @author John Cameron
 */
public class LocaleHelper {

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class CodeTranslation {

        /**
         * Standard code of translated item - eg ISO language code or country code
         */
        private String code;

        /**
         * Translated display name.
         */
        private String translation;
    }

    /**
     * Returns translations of all known countries in the given language
     * @param langCode Code of language - eg fr for French
     * @return Translations together with the country codes - may be empty
     */
    public static @NotNull List<CodeTranslation> getCountryNameTranslations(@Nullable String langCode) {
        List<CodeTranslation> cts = new ArrayList<>();
        if (langCode != null && isKnownLanguageCode(langCode)) {
            Locale translationLocale = new Locale(langCode);
            String[] countryCodes = Locale.getISOCountries();
            for (String countryCode : countryCodes) {
                Locale countryLocale = new Locale("", countryCode);
                String name = countryLocale.getDisplayCountry(translationLocale);
                cts.add(new CodeTranslation(countryCode, name));
            }
        }
        return cts;
    }

    /**
     * Returns translations of all known languages in the given language
     * @param langCode Code of language - eg fr for French
     * @return Translations together with the language codes - may be empty
     */
    public static @NotNull List<CodeTranslation> getLanguageNameTranslations(@Nullable String langCode) {
        List<CodeTranslation> cts = new ArrayList<>();
        if (langCode != null && isKnownLanguageCode(langCode)) {
            Locale translationLocale = new Locale(langCode);
            String[] languageCodes = Locale.getISOLanguages();
            for (String languageCode : languageCodes) {
                Locale languageLocale = new Locale(languageCode);
                String name = languageLocale.getDisplayLanguage(translationLocale);
                cts.add(new CodeTranslation(languageCode, name));
            }
        }
        return cts;
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
