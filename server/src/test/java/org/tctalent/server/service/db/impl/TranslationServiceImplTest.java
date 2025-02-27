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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class TranslationServiceImplTest {

    @Autowired
    TranslationServiceImpl translationService;

    @Test
    void getTranslationFile() {
        final Map<String, Object> translations = translationService.getTranslationFile("en");
        assertNotNull(translations);
    }

    @Test
    void translate() {
        final Map<String, Object> translations = translationService.getTranslationFile("en");
        assertNotNull(translations);

        String translation;

        translation = translationService.translate(translations,
            "CASE-STAGE", "ACCEPTANCE");
        assertNotNull(translation);

        //Case doesn't matter here
        translation = translationService.translate(translations,
            "case-stage", "acceptance");
        assertNotNull(translation);

        //Non existent return null
        translation = translationService.translate(translations,
            "CASE-STAGE", "___XXXX");
        assertNull(translation);
    }

    @Test
    void translateToEnglish() {
        String translation;

        translation = translationService.translateToEnglish(
            "CASE-STAGE", "ACCEPTANCE");
        assertNotNull(translation);

        //Case doesn't matter here
        translation = translationService.translateToEnglish(
            "case-stage", "acceptance");
        assertNotNull(translation);

        //Non existent return null
        translation = translationService.translateToEnglish(
            "CASE-STAGE", "___XXXX");
        assertNull(translation);
    }
}
