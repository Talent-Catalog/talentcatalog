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
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Candidate;

/**
 * Values can come from one of two sources:
 * <ul>
 *   <li>The field of a candidate object</li>
 *   <li>A constant value</li>
 * </ul>
 * <p/>
 * It has to be one or other - it cannot be both. If the field name is not null, that is what is
 * used, otherwise the constant is used.
 * <p/>
 * Note that this could be better designed in Java with a value source interface with two 
 * implementations: one a field source and the other a constant source. Unfortunately that 
 * does not map to JSON well - which we need to in order to map to send up to Angular Typescript. 
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocValueSource {
  private static final Logger log = LoggerFactory.getLogger(PublishedDocValueSource.class);

  /**
   * If not null, this is the name of a candidate field which is used to extract the value from
   * a candidate object.
   * If it is null, the value comes from {@link #constant}
   */
  @Nullable
  private String fieldName;

  /**
   * Ignored if {@link #fieldName} is not null. Otherwise it supplies the value.
   */
  @Nullable
  private Object constant;

  /**
   * Retrieves the data that is the value of this value source corresponding to the given candidate.
   * @param candidate Candidate - only used for field value sources 
   * @return the value
   */
  @Nullable
  public Object fetchData(Candidate candidate) {
    Object val = null;
    if (fieldName != null) {
      if (candidate == null) {
        log.error("Cannot extract field " + fieldName + " from null candidate");
      } else {
        try {
          // Get the list specific shareable CV or Doc if exists, otherwise get the field name supplied.
          if (fieldName.equals("shareableCv.url") && candidate.getListShareableCv() != null) {
            val = candidate.extractField("listShareableCv.url");
          } else if (fieldName.equals("shareableDoc.url") && candidate.getListShareableDoc() != null) {
            val = candidate.extractField("listShareableDoc.url");
          } else {
            val = candidate.extractField(fieldName);
          }
        } catch (Exception e) {
          log.error("Error extracting field " + fieldName + " from candidate " + candidate.getCandidateNumber());
        }
      }
    } else {
      val = constant;
    }
    return val;
  }
  
}
