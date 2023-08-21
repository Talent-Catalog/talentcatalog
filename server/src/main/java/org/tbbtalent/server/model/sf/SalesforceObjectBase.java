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

package org.tbbtalent.server.model.sf;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.tbbtalent.server.configuration.SalesforceConfig;

import javax.annotation.PostConstruct;

/**
 * Base class for Salesforce objects
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public abstract class SalesforceObjectBase {
  static String urlRoot;
  static final String urlSuffix = "/view";

  @Autowired
  private SalesforceConfig salesforceConfig;

  /**
   * PostConstruct (baeldung.com/spring-postconstruct-predestroy) populates the urlRoot field right after initialisation using the SalesforceConfig dependency - this gets around having to change the constructor of this class and thereby it's SF object subclasses, which gets a little tricky.
   */
  @PostConstruct
  private void initialize() {
    urlRoot = this.salesforceConfig.getBaseLightningUrl() + "/lightning/r/";
  }

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

  public String getUrl() {
    String url = null;
    if (id != null) {
      url = urlRoot + getSfObjectName() + "/" + id + urlSuffix;
    }
    return url;
  }
}
