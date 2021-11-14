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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
      private String code;
      private String translation;
    }

    public List<CodeTranslation> getCountryNameTranslations(String language) {
        Locale translationLocale = new Locale(language);
        List<CodeTranslation> cts = new ArrayList<>();
        String[] countryCodes = Locale.getISOCountries();
        for (String countryCode : countryCodes) {
            Locale countryLocale = new Locale("", countryCode);
            String name = countryLocale.getDisplayCountry(translationLocale);
            cts.add(new CodeTranslation(countryCode, name));
        }
        return cts;
    }

    public List<CodeTranslation> getLanguageNameTranslations(String language) {
        Locale translationLocale = new Locale(language);
        List<CodeTranslation> cts = new ArrayList<>();
        String[] languageCodes = Locale.getISOLanguages();
        for (String languageCode : languageCodes) {
            Locale languageLocale = new Locale(languageCode);
            String name = languageLocale.getDisplayLanguage(translationLocale);
            cts.add(new CodeTranslation(languageCode, name));
        }
        return cts;
    }

}
