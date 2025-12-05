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

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.dto.DtoType;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.PartnerDtoHelper;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.PublicApiPartnerDto;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.request.partner.UpdatePartnerJobContactRequest;
import org.tctalent.server.request.partner.UpdatePartnerRequest;
import org.tctalent.server.service.db.EmployerService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;

@RestController
@RequestMapping("/api/admin/partner")
@RequiredArgsConstructor
public class PartnerAdminApi implements
        ITableApi<SearchPartnerRequest, UpdatePartnerRequest, UpdatePartnerRequest> {

    private final EmployerService employerService;
    private final PartnerService partnerService;
    private final JobService jobService;
    private final UserService userService;

    @Override
    public @NotNull Map<String, Object> create(UpdatePartnerRequest request)
        throws EntityExistsException, NoSuchObjectException {

        Employer employer = extractEmployerFromUpdatePartnerRequest(request);
        request.setEmployer(employer);

        Partner partner = partnerService.create(request);
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    @Override
    public @NotNull Map<String, Object> get(long id, DtoType dtoType) throws NoSuchObjectException {
        Partner partner = partnerService.getPartner(id);
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    @Override
    public @NotNull List<Map<String, Object>> list() {
        List<PartnerImpl> partners = partnerService.listPartners();
        return PartnerDtoHelper.getPartnerDto().buildList(partners);
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
    public @NotNull Map<String, Object> update(long id, @Valid UpdatePartnerRequest request)
            throws EntityExistsException, NoSuchObjectException {

        //Note - have to look up contact user here rather than in partnerService to avoid
        //circular dependency between partnerService and userService. Spring doesn't like that.
        final Long defaultContactId = request.getDefaultContactId();
        User defaultContact = defaultContactId == null ? null : userService.getUser(defaultContactId);
        request.setDefaultContact(defaultContact);

        //Populate Employer from data in request
        Employer employer = extractEmployerFromUpdatePartnerRequest(request);
        request.setEmployer(employer);

        Partner partner = partnerService.update(id, request);

        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SYSTEMADMIN')")
    @GetMapping("public-api-key/{api-key}")
    public ResponseEntity<PublicApiPartnerDto> findPartnerByPublicApiKey(@PathVariable("api-key") String apiKey) {
        PublicApiPartnerDto partner = partnerService.findPublicApiPartnerDtoByKey(apiKey);
        return ResponseEntity.ok(partner);
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

    @Nullable
    private Employer extractEmployerFromUpdatePartnerRequest(UpdatePartnerRequest request)
        throws NoSuchObjectException {
        Employer employer = null;
        final String employerSflink = request.getEmployerSflink();
        if (employerSflink != null) {
            employer = employerService.findOrCreateEmployerFromSalesforceLink(employerSflink);
        }
        return employer;
    }

    /**
     * Marks the partner as having accepted the Data Processing Agreement (DPA).
     *
     * @param id The partner ID
     * @return The updated partner information
     */
    @PutMapping("{id}/accept-dpa")
    public Map<String, Object> acceptDpa(@PathVariable("id") String id) {
        Partner partner = partnerService.updateAcceptedDpa(id);
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    /**
     * Records the first time the partner has seen the Data Processing Agreement (DPA).
     *
     * @return The updated partner information
     */
    @PutMapping("/dpa-seen")
    public Map<String, Object> setFirstDpaSeen() {
        PartnerImpl partner = partnerService.setFirstDpaSeen();
        return PartnerDtoHelper.getPartnerDto().build(partner);
    }

    /**
     * Checks whether the partner still needs to accept the Data Processing Agreement (DPA).
     *
     * @return true if the partner is required to accept the DPA, false otherwise
     */
    @GetMapping("/requires-dpa")
    public boolean requiresDpaAcceptance() {
        return partnerService.requiresDpaAcceptance();
    }

}
