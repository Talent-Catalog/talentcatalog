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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.model.db.partner.SourcePartner;
import org.tbbtalent.server.request.partner.SearchPartnerRequest;
import org.tbbtalent.server.request.partner.UpdatePartnerRequest;
import org.tbbtalent.server.service.db.PartnerService;
import org.tbbtalent.server.util.dto.DtoBuilder;
import org.tbbtalent.server.util.dto.DtoPropertyFilter;

@RestController()
@RequestMapping("/api/admin/partner")
public class PartnerAdminApi implements
        ITableApi<SearchPartnerRequest, UpdatePartnerRequest, UpdatePartnerRequest> {

    private final PartnerService partnerService;

    @Autowired
    public PartnerAdminApi(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @Override
    public @NotNull Map<String, Object> create(UpdatePartnerRequest request) throws EntityExistsException {
        Partner partner = partnerService.create(request);
        return partnerDto().build(partner);
    }

    @Override
    public @NotNull Map<String, Object> get(long id) throws NoSuchObjectException {
        Partner partner = partnerService.getPartner(id);
        return partnerDto().build(partner);
    }

    @Override
    public @NotNull List<Map<String, Object>> search(@Valid SearchPartnerRequest request) {
        List<PartnerImpl> partners = partnerService.search(request);
        return partnerDto().buildList(partners);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(@Valid SearchPartnerRequest request) {
        Page<PartnerImpl> partners = partnerService.searchPaged(request);
        return partnerDto().buildPage(partners);
    }

    @Override
    public @NotNull Map<String, Object> update(
            long id, @Valid UpdatePartnerRequest request)
            throws EntityExistsException, NoSuchObjectException {
        Partner partner = partnerService.update(id, request);
        return partnerDto().build(partner);
    }

    /**
     * Filters out properties in the DtoBuilder not appropriate to the type of partner
     */
    static private class PartnerDtoPropertyFilter implements DtoPropertyFilter {

        //These properties should only be extracted for source partner's
        private final Set<String> sourcePartnerOnlyProperties =
            new HashSet<>(Arrays.asList(
                "registrationLandingPage", "sourceCountries", "defaultSourcePartner",
                "autoAssignable", "defaultPartnerRef"));

        public boolean ignoreProperty(Object o, String property) {
            //Ignore properties which do not exist on type of partner
            boolean ignore =
                sourcePartnerOnlyProperties.contains(property) && ! (o instanceof SourcePartner);

            return ignore;
        }
    };

    private DtoBuilder partnerDto() {
        return new DtoBuilder( new PartnerDtoPropertyFilter() )
            .add("abbreviation")
            .add("autoAssignable")
            .add("defaultSourcePartner")
            .add("defaultPartnerRef")
            .add("id")
            .add("logo")
            .add("name")
            .add("notificationEmail")
            .add("partnerType")
            .add("status")
            .add("websiteUrl")
            .add("registrationLandingPage")
            .add("sflink")
            .add("sourceCountries", countryDto())
            ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

}
