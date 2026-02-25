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

package org.tctalent.server.casi.application.providers.linkedin;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.service.db.SavedListService;

@Service
public class LinkedInService extends AbstractCandidateAssistanceService {
    private final FileInventoryImporter linkedInImporter;
    private final ResourceAllocator linkedInAllocator;

    public LinkedInService(
        ServiceAssignmentRepository aRepo,
        ServiceResourceRepository rRepo,
        AssignmentEngine engine,
        SavedListService lists,
        @Qualifier("linkedInCouponImporter") FileInventoryImporter importer,
        @Qualifier("linkedInPremiumMembershipAllocator") ResourceAllocator allocator
    ) {
        super(aRepo, rRepo, engine, lists);
        this.linkedInAllocator = allocator;
        this.linkedInImporter = importer;
    }

    @Override protected ServiceProvider provider() { return ServiceProvider.LINKEDIN; }
    @Override protected ServiceCode serviceCode() { return ServiceCode.PREMIUM_MEMBERSHIP; }
    @Override protected ResourceAllocator allocator() { return linkedInAllocator; }
    @Override protected FileInventoryImporter importer() { return linkedInImporter; }
}
