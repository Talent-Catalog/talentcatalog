/*
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


package org.tctalent.server.request.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.RefugeeStatusEvidenceDocumentType;
import org.tctalent.server.model.db.RsdRefugeeStatus;

/**
 * Payload captured by the RSD evidence form.
 */
@Getter
@Setter
public class RefugeeStatusInfoFormData {
  @NotNull
  private RsdRefugeeStatus refugeeStatus;

  @NotNull
  private RefugeeStatusEvidenceDocumentType documentType;

  @NotBlank
  @Size(max = 30)
  private String documentNumber;

  private String refugeeStatusComment;
}
