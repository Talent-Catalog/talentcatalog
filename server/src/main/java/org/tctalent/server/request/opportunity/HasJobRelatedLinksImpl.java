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

package org.tctalent.server.request.opportunity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base class for objects containing job related links.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class HasJobRelatedLinksImpl {

  /**
   * Link to Salesforce employer job opportunity
   */
  @NotBlank
  private String sfJoblink;
  /**
   * Link to associated Talent Catalog list
   */
  private String listlink;

  /**
   * Link to associated job description file
   */
  private String fileJdLink;

  /**
   * Name of associated job description file
   */
  private String fileJdName;

  /**
   * Link to associated job opportunity intake file
   */
  private String fileJoiLink;

  /**
   * Name of associated job opportunity intake file
   */
  private String fileJoiName;

  /**
   * Link to associated Google Drive root folder
   */
  private String folderlink;

  /**
   * Link to associated Google Drive Job Description subfolder
   */
  private String folderjdlink;

  /**
   * TC id of Job entity
   */
  private Long jobId;
}
