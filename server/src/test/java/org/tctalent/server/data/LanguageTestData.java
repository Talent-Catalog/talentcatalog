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

package org.tctalent.server.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.model.db.Translation;

public class LanguageTestData {

    public static LanguageLevel getLanguageLevel() {
        LanguageLevel languageLevel = new LanguageLevel(
                "Excellent", Status.active, 1
        );
        return languageLevel;
    }

    public static List<LanguageLevel> getLanguageLevelList() {
        return List.of(
                getLanguageLevel()
        );
    }

    public static SystemLanguage getSystemLanguage() {
        SystemLanguage systemLanguage = new SystemLanguage(
                "Spanish"
        );
        systemLanguage.setId(1L);
        return systemLanguage;
    }

    public static Language getLanguage() {
        Language language = new Language(
                "Arabic", Status.active
        );
        language.setId(99L);
        return language;
    }

    public static List<Language> getLanguageList() {
        return List.of(
                getLanguage()
        );
    }

    public static List<SystemLanguage> getSystemLanguageList() {
        return List.of(
                getSystemLanguage()
        );
    }

    public static Translation getTranslation() {
        Translation trans = new Translation(
                UserTestData.getAuditUser(),
                1L,
                "Country",
                "French",
                "Australie"
        );
        return trans;
    }

    public static Map<String, Object> getTranslationFile() {
        Map<String, Object> map = new HashMap<>();
        map.put("my key", "my value");
        return map;
    }
}
