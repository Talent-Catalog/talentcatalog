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

package org.tctalent.server.casi.application.providers.linkedin;

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
import lombok.extern.slf4j.Slf4j;
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
import org.tctalent.server.logging.LogBuilder;

/**
 * Imports LinkedIn Premium coupons from a CSV file into the system.
 * The CSV is expected to have the following columns:
 * - serial #
 * - premium code
 * - activate by
 * <p>
 * The importer checks for duplicates within the file and against existing records in the database.
 * It supports multiple date formats for parsing dates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LinkedInCouponImporter implements FileInventoryImporter {

    private final ServiceResourceRepository serviceResourceRepository;

    private static final ServiceProvider PROVIDER = ServiceProvider.LINKEDIN;

    // Date format not clear yet from the example CSV
    private static final DateTimeFormatter FORMATTER1 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    @Transactional
    public void importFile(MultipartFile file, ServiceCode serviceCode) throws ImportFailedException {
        List<ServiceResourceEntity> newCoupons = new ArrayList<>();
        // Set to track coupon codes that have already been processed (avoids duplicates)
        Set<String> seenCouponCodes = new HashSet<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // Parse the header of the CSV to ensure it is valid
            String[] headers = parseCsvHeader(reader);

            // Map column names to their respective indices for easy access
            Map<String, Integer> columnIndex = mapColumnsToIndex(headers);

            // Validate that all required columns are present in the CSV
            validateRequiredColumns(columnIndex);

            // Read each line of the CSV and process coupon data
            String[] line;
            while ((line = reader.readNext()) != null) {
                processCouponLine(line, headers, columnIndex, seenCouponCodes, newCoupons);
            }

            // Save all the new coupons to the repository if there are any
            saveCoupons(newCoupons);

        } catch (ImportFailedException e) {
            // Re-throw ImportFailedException as-is (already wrapped)
            throw e;
        } catch (IOException | CsvValidationException | RuntimeException e) {
            // Catch any other exceptions related to the import process and wrap them
            throw new ImportFailedException(e);
        }
    }

    // Parses the CSV header row to ensure the file has valid headers.
    private String[] parseCsvHeader(CSVReader reader)
        throws ImportFailedException, CsvValidationException, IOException {
        // Read the first row (header) from the CSV
        String[] headers = reader.readNext();

        // If no headers are found, throw an exception
        if (headers == null) {
            throw new ImportFailedException("CSV header is missing");
        }
        return headers;
    }

    // Maps column names to their respective indices based on the CSV header.
    private Map<String, Integer> mapColumnsToIndex(String[] headers) {
        Map<String, Integer> columnIndex = new HashMap<>();

        // Normalize header names (lowercase, strip whitespace, remove BOM) and map them to their indices
        for (int i = 0; i < headers.length; i++) {
            String normalizedHeader = headers[i].toLowerCase().replace("\uFEFF", "").strip();
            columnIndex.put(normalizedHeader, i);
        }
        return columnIndex;
    }
    // Validates that all required columns are present in the CSV file.
    private void validateRequiredColumns(Map<String, Integer> columnIndex) throws ImportFailedException {
        // List of required columns that must be present in the CSV
        String[] requiredColumns = {"serial #", "premium code", "activate by"};

        // Check if all required columns are in the CSV
        for (String column : requiredColumns) {
            if (!columnIndex.containsKey(column)) {
                throw new ImportFailedException("Missing required column: " + column);
            }
        }
    }

    /**
     * Processes each coupon line from the CSV and adds it to the newCoupons list if it's valid.
     * Coupons with unrecognized prefixes are logged and skipped.
     * @param line the current line from the CSV
     * @param headers the array of headers from the CSV
     * @param columnIndex the map of column names to their indices
     * @param seenCouponCodes a set to track processed coupon codes
     * @param newCoupons the list where valid coupons will be added
     */
    private void processCouponLine(String[] line, String[] headers, Map<String, Integer> columnIndex,
        Set<String> seenCouponCodes, List<ServiceResourceEntity> newCoupons) {
        // Ensure the line has enough columns to process
        if (line.length >= headers.length) {
            // Extract the coupon code from the current line
            String couponCode = line[columnIndex.get("premium code")];

            // Check if the coupon code has already been processed
            if (!seenCouponCodes.contains(couponCode)) {
                seenCouponCodes.add(couponCode); // Mark this coupon code as processed

                // If the coupon code does not exist in the database, create a new coupon
                if (!serviceResourceRepository.existsByProviderAndResourceCode(PROVIDER, couponCode)) {
                    ServiceResourceEntity coupon = new ServiceResourceEntity();
                    coupon.setProvider(PROVIDER);
                    coupon.setResourceCode(couponCode);
                    coupon.setExpiresAt(parseDate(line[columnIndex.get("activate by")], FORMATTER1, FORMATTER2));
                    coupon.setStatus(ResourceStatus.AVAILABLE);
                    coupon.setServiceCode(ServiceCode.PREMIUM_MEMBERSHIP);
                    newCoupons.add(coupon);
                }
            }
        }
    }

    /**
     * Attempts to parse a date using multiple formatters.
     */
    private OffsetDateTime parseDate(String dateString, DateTimeFormatter... formatters) {
        if (dateString != null && !dateString.trim().isEmpty()) {
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateString, formatter)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);
                } catch (DateTimeParseException ex) {
                    LogBuilder.builder(log)
                        .action("parseDate")
                        .message(String.format("Failed to parse date '%s' with formatter '%s'. Exception: %s",
                            dateString, formatter, ex.getMessage()))
                        .logError(ex);

                }
            }
            throw new RuntimeException("Invalid date format: " + dateString);
        }
        return null;
    }

    /**
     * Saves the new coupons to the database if any valid coupons exist.
     */
    private void saveCoupons(List<ServiceResourceEntity> newCoupons) {
        if (!newCoupons.isEmpty()) {
            serviceResourceRepository.saveAll(newCoupons);
        }
    }

}
