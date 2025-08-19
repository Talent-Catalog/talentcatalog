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

package org.tctalent.server.model.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tctalent.server.response.MetadataFieldResponse;

@Converter
@Slf4j
public class MetadataFieldConverter implements AttributeConverter<List<MetadataFieldResponse>, String> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<MetadataFieldResponse> metadataFields) {
    try {
      return metadataFields == null ? null : objectMapper.writeValueAsString(metadataFields);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize MetadataField list to JSON", e);
      throw new IllegalStateException("Cannot convert MetadataField list to JSON", e);
    }
  }

  @Override
  public List<MetadataFieldResponse> convertToEntityAttribute(String json) {
    try {
      return json == null ? null : objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, MetadataFieldResponse.class));
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize JSON to MetadataField list", e);
      throw new IllegalStateException("Cannot convert JSON to MetadataField list", e);
    }
  }
}