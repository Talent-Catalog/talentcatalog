/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.casi.application.providers.reference;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.ImportFailedException;

/**
 * Minimal CSV importer for REFERENCE::VOUCHER.
 * Expected headers: voucher_code, expires_at
 *
 * @author sadatmalik
 */
@Component
@RequiredArgsConstructor
public class ReferenceVoucherImporter implements FileInventoryImporter {

  private static final ServiceProvider PROVIDER = ServiceProvider.REFERENCE;
  private static final String RESOURCE_CODE_HEADER = "voucher_code";
  private static final String EXPIRES_AT_HEADER = "expires_at";
  private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
      DateTimeFormatter.ofPattern("yyyy-MM-dd"),
      DateTimeFormatter.ofPattern("dd/MM/yyyy"),
      DateTimeFormatter.ofPattern("MM/dd/yyyy")
  };

  private final ServiceResourceRepository resources;

  @Override
  @Transactional
  public void importFile(MultipartFile file, ServiceCode serviceCode) throws ImportFailedException {
    List<ServiceResourceEntity> created = new ArrayList<>();
    Set<String> seenCodes = new HashSet<>();

    try (CSVReader reader = new CSVReader(
        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String[] headers = reader.readNext();
      if (headers == null) {
        throw new ImportFailedException("CSV header is missing");
      }

      Map<String, Integer> index = mapColumns(headers);
      validateRequired(index);

      String[] line;
      while ((line = reader.readNext()) != null) {
        if (line.length < headers.length) {
          continue;
        }

        String code = value(line, index.get(RESOURCE_CODE_HEADER));
        if (code == null || !seenCodes.add(code)) {
          continue;
        }
        if (resources.existsByProviderAndResourceCode(PROVIDER, code)) {
          continue;
        }

        ServiceResourceEntity entity = new ServiceResourceEntity();
        entity.setProvider(PROVIDER);
        entity.setServiceCode(ServiceCode.VOUCHER);
        entity.setResourceCode(code);
        entity.setStatus(ResourceStatus.AVAILABLE);
        entity.setExpiresAt(parseDate(value(line, index.get(EXPIRES_AT_HEADER))));
        created.add(entity);
      }

      if (!created.isEmpty()) {
        resources.saveAll(created);
      }
    } catch (ImportFailedException e) {
      throw e;
    } catch (IOException | CsvValidationException | RuntimeException e) {
      throw new ImportFailedException(e);
    }
  }

  private static Map<String, Integer> mapColumns(String[] headers) {
    Map<String, Integer> map = new HashMap<>();
    for (int i = 0; i < headers.length; i++) {
      map.put(headers[i].replace("\uFEFF", "").trim().toLowerCase(), i);
    }
    return map;
  }

  private static void validateRequired(Map<String, Integer> index) {
    if (!index.containsKey(RESOURCE_CODE_HEADER)) {
      throw new ImportFailedException("Missing required column: " + RESOURCE_CODE_HEADER);
    }
    if (!index.containsKey(EXPIRES_AT_HEADER)) {
      throw new ImportFailedException("Missing required column: " + EXPIRES_AT_HEADER);
    }
  }

  private static String value(String[] line, int i) {
    if (i >= line.length) {
      return null;
    }
    String v = line[i];
    if (v == null) {
      return null;
    }
    String normalized = v.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private static OffsetDateTime parseDate(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    for (DateTimeFormatter f : DATE_FORMATS) {
      try {
        return LocalDate.parse(raw, f).atStartOfDay().atOffset(ZoneOffset.UTC);
      } catch (DateTimeParseException ignored) {
      }
    }
    throw new RuntimeException("Invalid date format: " + raw);
  }
}
