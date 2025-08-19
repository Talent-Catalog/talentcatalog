package org.tctalent.server.util.validator;/*
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

import java.util.HashMap;
import java.util.Map;


public class MetadataValidatorFactory {
  private static final Map<String, MetadataValidator> validators = new HashMap<>();

  static {
    validators.put("candidateTravelDocumentUpload", new CandidateTravelDocumentMetadataValidator());
    // Add more validators for other task types as needed
  }

  public static MetadataValidator getValidator(String taskName) {
    return validators.getOrDefault(taskName, new DefaultMetadataValidator());
  }
}