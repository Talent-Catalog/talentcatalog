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

package org.tctalent.server.request.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.request.PagedSearchRequest;

@Getter
@Setter
public class FetchCandidatesWithChatRequest extends PagedSearchRequest {

  /**
   * Used to match candidates whose first or last names are like this keyword —
   * empty string received if no value entered in search form.
   */
  @NotNull
  private String keyword;

  /**
   * When true, returns matching candidates whose chat has posts unread by the logged-in user.
   */
  @NotNull
  private boolean unreadOnly;

}
