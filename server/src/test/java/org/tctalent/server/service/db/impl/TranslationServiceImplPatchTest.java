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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.repository.db.TranslationRepository;
import org.tctalent.server.request.translation.ExportTranslationPatchRequest;
import org.tctalent.server.request.translation.TranslationPatchEntry;
import org.tctalent.server.request.translation.TranslationPatchRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.storage.S3TranslationStorageService;

@ExtendWith(MockitoExtension.class)
class TranslationServiceImplPatchTest {

    @Mock
    TranslationRepository translationRepository;
    @Mock
    S3TranslationStorageService s3TranslationStorageService;
    @Mock
    Environment environment;
    @Mock
    AuthService authService;

    TranslationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TranslationServiceImpl(
            translationRepository, s3TranslationStorageService, environment, authService);
    }

    @Test
    void importPatch_dryRun_doesNotWriteToS3() {
        when(s3TranslationStorageService.getTranslationFile("en")).thenReturn(new LinkedHashMap<>(Map.of(
            "SERVICES", new LinkedHashMap<>(Map.of(
                "VERIFY_PLUS", new LinkedHashMap<>(Map.of("TITLE", "UNHCR Verify+"))
            ))
        )));
        when(s3TranslationStorageService.getActiveTranslationsBucket()).thenReturn("translations.test");

        TranslationPatchRequest request = new TranslationPatchRequest();
        request.setVersion(1);
        request.setLanguages(List.of("en"));
        request.setEntries(List.of(entry(
            "SERVICES.VERIFY_PLUS.TITLE", Map.of("en", "UNHCR Verify+ Updated"))));

        Map<String, Object> result = service.importTranslationPatch(request, true, false);

        assertEquals("success", result.get("status"));
        assertEquals(true, result.get("dryRun"));
        Map<?, ?> languageReport = (Map<?, ?>) ((Map<?, ?>) result.get("languages")).get("en");
        assertEquals(1, languageReport.get("updatedKeys"));
        assertEquals(0, languageReport.get("unchangedKeys"));
        assertEquals(false, languageReport.get("wroteFile"));
        verify(s3TranslationStorageService, never()).updateTranslationFile(any(), any());
    }

    @Test
    void importPatch_apply_writesMergedFileToS3() {
        when(s3TranslationStorageService.getTranslationFile("en")).thenReturn(new LinkedHashMap<>(Map.of(
            "SERVICES", new LinkedHashMap<>(Map.of(
                "VERIFY_PLUS", new LinkedHashMap<>(Map.of(
                    "TITLE", "UNHCR Verify+",
                    "TAG", "Verify+"
                ))
            ))
        )));
        when(s3TranslationStorageService.getActiveTranslationsBucket()).thenReturn("translations.test");

        TranslationPatchRequest request = new TranslationPatchRequest();
        request.setVersion(1);
        request.setLanguages(List.of("en"));
        request.setEntries(List.of(entry(
            "SERVICES.VERIFY_PLUS.TITLE", Map.of("en", "UNHCR Verify+ Updated"))));

        service.importTranslationPatch(request, false, false);

        verify(s3TranslationStorageService).updateTranslationFile(eq("en"), any());
    }

    @Test
    void importPatch_strictLanguages_rejectsMissingLanguageValue() {
        TranslationPatchRequest request = new TranslationPatchRequest();
        request.setVersion(1);
        request.setLanguages(List.of("en", "ar"));
        request.setEntries(List.of(entry(
            "SERVICES.VERIFY_PLUS.TAG", Map.of("en", "Verify+"))));

        assertThrows(InvalidRequestException.class,
            () -> service.importTranslationPatch(request, true, true));
    }

    @Test
    void exportPatch_returnsOnlyScopedKeys() {
        Map<String, Object> enFile = new LinkedHashMap<>();
        TranslationPatchUtils.setNestedValue(enFile, "SERVICES.VERIFY_PLUS.TAG", "Verify+");
        TranslationPatchUtils.setNestedValue(enFile, "SERVICES.VERIFY_PLUS.TITLE", "UNHCR Verify+");
        TranslationPatchUtils.setNestedValue(enFile, "SERVICES.UNHCR.TITLE", "UNHCR Help");

        Map<String, Object> arFile = new LinkedHashMap<>();
        TranslationPatchUtils.setNestedValue(arFile, "SERVICES.VERIFY_PLUS.TAG", "Verify+");
        TranslationPatchUtils.setNestedValue(arFile, "SERVICES.VERIFY_PLUS.TITLE", "UNHCR Verify+");
        TranslationPatchUtils.setNestedValue(arFile, "SERVICES.UNHCR.TITLE", "UNHCR مساعدة");

        when(s3TranslationStorageService.getTranslationFile("en")).thenReturn(enFile);
        when(s3TranslationStorageService.getTranslationFile("ar")).thenReturn(arFile);
        when(s3TranslationStorageService.getActiveTranslationsBucket()).thenReturn("translations.test");

        ExportTranslationPatchRequest request = new ExportTranslationPatchRequest();
        request.setLanguages(List.of("en", "ar"));
        request.setPrefixes(List.of("SERVICES.VERIFY_PLUS"));
        request.setKeys(List.of("SERVICES.UNHCR.TITLE"));

        Map<String, Object> exported = service.exportTranslationPatch(request);

        assertEquals(1, exported.get("version"));
        assertEquals(List.of("ar", "en"), exported.get("languages"));
        assertNotNull(exported.get("entries"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) exported.get("entries");
        assertFalse(entries.isEmpty());
        verify(s3TranslationStorageService, never()).updateTranslationFile(any(), any());
    }

    @Test
    void exportPatch_rejectsEmptyScope() {
        ExportTranslationPatchRequest request = new ExportTranslationPatchRequest();
        request.setLanguages(List.of("en"));

        assertThrows(InvalidRequestException.class, () -> service.exportTranslationPatch(request));
    }

    private static TranslationPatchEntry entry(String key, Map<String, String> values) {
        TranslationPatchEntry entry = new TranslationPatchEntry();
        entry.setKey(key);
        entry.setValues(values);
        return entry;
    }
}
