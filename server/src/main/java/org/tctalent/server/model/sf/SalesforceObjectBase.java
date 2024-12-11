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

package org.tctalent.server.model.sf;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base class for Salesforce objects
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public abstract class SalesforceObjectBase {
  static final String urlMiddle = "/lightning/r/";
  static final String urlSuffix = "/view";

  /**
   * This is the Salesforce Id that every Salesforce record has.
   */
  @JsonSetter("Id")
  private String id;

  /**
   * This is the name of this class of object as referred to in SF urls
   * @return Name object used in urls - eg Contact or Opportunity
   */
  abstract String getSfObjectName();

  public String getUrl(String baseLightningUrl) {
    String url = null;
    if (id != null) {
      url = baseLightningUrl + urlMiddle + getSfObjectName() + "/" + id + urlSuffix;
    }
    return url;
  }
}
