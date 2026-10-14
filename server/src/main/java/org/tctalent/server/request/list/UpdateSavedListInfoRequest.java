/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.request.list;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;
import org.tctalent.server.service.db.SavedListService;

/**
 * Request for modifying just the info associated with a SavedList (not the content)
 * - eg changing the name
 * <p>
 * Only for ordinary Saved Lists: it cannot make a list a Submission List
 * (see {@link SavedList#getRegisteredJob()}). A job's Submission List is only created by the
 * job workflow - see {@link SavedListService#getOrCreateSubmissionList}.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateSavedListInfoRequest extends AbstractUpdateCandidateSourceRequest {
}
