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
import org.tbbtalent.server.model.db.Candidate;

/**
 * Source of data which is a fixed constant 
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocConstantSource implements IValueSource {

  /**
   * This is the constant value always returned
   */
  private Object value;

  public PublishedDocConstantSource() {
  }

  public PublishedDocConstantSource(Object value) {
    this.value = value;
  }

  /**
   * Returns constant value
   * @param candidate Ignore
   * @return Constant value
   */
  @Override
  public Object fetchValue(Candidate candidate) {
    return value;
  }
}
