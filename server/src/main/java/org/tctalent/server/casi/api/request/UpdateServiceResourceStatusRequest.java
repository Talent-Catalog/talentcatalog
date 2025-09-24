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

package org.tctalent.server.casi.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.casi.domain.model.ResourceStatus;

/**
 * Represents a request to update the status of a Service Resource.
 * This request includes the resource code and the new status to be assigned.
 */
@Getter
@Setter
public class UpdateServiceResourceStatusRequest {
  /**
   * The unique code of the service resource to be updated.
   * This field must not be blank.
   */
  @NotBlank(message = "Resource code must not be blank")
  private String resourceCode;
  /**
   * The new status to be assigned to the service resource.
   * This field must not be blank.
   */
  @NotBlank(message = "Status must not be blank")
  private ResourceStatus status;

}
