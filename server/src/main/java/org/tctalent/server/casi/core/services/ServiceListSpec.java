/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.casi.core.services;

import java.util.Set;
import org.tctalent.server.casi.domain.model.ListAction;
import org.tctalent.server.casi.domain.model.ListRole;

/**
 * Declares a single service list that a {@link CandidateAssistanceService} requires.
 * <p>
 * A service implementation returns a list of these from its {@code serviceListSpecs()} method.
 * The {@link ServiceListSetupService} reads these specs at startup and creates the corresponding
 * saved list and {@link org.tctalent.server.casi.domain.persistence.ServiceListEntity} if they
 * do not already exist.
 *
 * @param listName         The display name for the saved list.
 * @param role             The semantic role this list plays within the service.
 * @param permittedActions The actions an admin may perform on candidates in this list.
 */
public record ServiceListSpec(
    String listName,
    ListRole role,
    Set<ListAction> permittedActions
) {}
