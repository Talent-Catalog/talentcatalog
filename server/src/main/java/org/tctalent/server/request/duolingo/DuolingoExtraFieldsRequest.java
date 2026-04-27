package org.tctalent.server.request.duolingo;/*
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DuolingoExtraFieldsRequest {
  private String certificateUrl;
  private String interviewUrl;
  private String verificationDate;
  private int percentScore;
  private int scale;
  private int literacySubscore;
  private int conversationSubscore;
  private int comprehensionSubscore;
  private int productionSubscore;
  private Long candidateExamId;
}
