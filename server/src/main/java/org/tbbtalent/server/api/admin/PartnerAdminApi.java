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

package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.PartnerDtoHelper;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.request.partner.SearchPartnerRequest;
import org.tbbtalent.server.request.partner.UpdatePartnerJobContactRequest;
import org.tbbtalent.server.request.partner.UpdatePartnerRequest;
import org.tbbtalent.server.service.db.JobService;
import org.tbbtalent.server.service.db.PartnerService;
import org.tbbtalent.server.service.db.UserService;

@RestController()
@RequestMapping("/api/admin/partner")
public class PartnerAdminApi implements
        ITableApi<SearchPartnerRequest, UpdatePartnerRequest, UpdatePartnerRequest> {

    private final PartnerService partnerService;
    private final JobService jobService;
    private final UserService userService;

    @Autowired
    public PartnerAdminApi(PartnerService partnerService, JobService jobService,
        UserService userService) {
        this.partnerService = partnerService;
        this.jobService = jobService;
        this.userService = userService;
    }

    @Override
    public @NotNull Map<String, Object> create(UpdatePartnerRequest request) throws EntityExistsException {
        Partner partner = partnerService.create(request);
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    @Override
    public @NotNull Map<String, Object> get(long id) throws NoSuchObjectException {
        Partner partner = partnerService.getPartner(id);
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    @Override
    public @NotNull List<Map<String, Object>> search(@Valid SearchPartnerRequest request) {
        List<PartnerImpl> partners = partnerService.search(request);
        if (request.getContextJobId() != null) {
            for (PartnerImpl partner : partners) {
                partner.setContextJobId(request.getContextJobId());
            }
        }
        return PartnerDtoHelper.getPartnerDto().buildList(partners);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(@Valid SearchPartnerRequest request) {
        Page<PartnerImpl> partners = partnerService.searchPaged(request);
        return PartnerDtoHelper.getPartnerDto().buildPage(partners);
    }

    @Override
    public @NotNull Map<String, Object> update(
        @PathVariable("id") long id, @Valid UpdatePartnerRequest request)
            throws EntityExistsException, NoSuchObjectException {
        Partner partner = partnerService.update(id, request);
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    @PutMapping("{id}/update-job-contact")
    public @NotNull Map<String, Object> updateJobContact(
        @PathVariable("id") long id, @Valid @RequestBody UpdatePartnerJobContactRequest request)
        throws EntityExistsException, NoSuchObjectException {
        Partner partner = partnerService.getPartner(id);
        SalesforceJobOpp job = jobService.getJob(request.getJobId());
        User contactUser = userService.getUser(request.getUserId());
        partnerService.updateJobContact(partner, job, contactUser);
        partner.setContextJobId(request.getJobId());
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

}
