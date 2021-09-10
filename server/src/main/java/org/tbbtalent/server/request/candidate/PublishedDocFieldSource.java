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

package org.tbbtalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbbtalent.server.model.db.Candidate;

/**
 * Source of data which corresponds to a particular candidate field
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocFieldSource implements IValueSource {
  private static final Logger log = LoggerFactory.getLogger(PublishedDocFieldSource.class);

  /**
   * The data comes from this field of a candidate
   */
  private String fieldName;

  public PublishedDocFieldSource() {
  }
  
  public PublishedDocFieldSource(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Returns the value of the field in the given candidate
   * @param candidate Candidate whose field value is returned. 
   * @return Field value. Null if candidate is null, or there is a problem retrieving the field.
   */
  public Object fetchValue(Candidate candidate) {
    Object value = null;
    if (candidate == null) {
      log.error("Cannot extract field " + fieldName + " from null candidate");
    } else {
      try {
        value = candidate.extractField(fieldName);
      } catch (Exception e) {
        log.error("Error extracting field " + fieldName + " from candidate " + candidate.getCandidateNumber());
      }
    }
    return value;
  }
}
