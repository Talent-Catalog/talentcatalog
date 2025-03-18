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

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.anonymization.model.OfferToAssistCandidates201Response;
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.request.KeywordPagedSearchRequest;
import org.tctalent.server.request.OfferToAssistRequest;
import org.tctalent.server.service.db.OfferToAssistService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * TC API for OTA's (Offers To Assist candidates)
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/ota")
@Slf4j
@RequiredArgsConstructor
public class OfferToAssistAdminApi {
    private final OfferToAssistService offerToAssistService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    @NonNull
    public ResponseEntity<OfferToAssistCandidates201Response>
        create(@Valid @RequestBody OfferToAssistRequest request) {

        //Create and store OTA
        OfferToAssist ota = offerToAssistService.createOfferToAssist(request);

        //Create response
        OfferToAssistCandidates201Response response =
            new OfferToAssistCandidates201Response().toBuilder()
                .offerId(ota.getPublicId())
                .message("Your offer has been successfully recorded.")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody KeywordPagedSearchRequest request) {
        Page<OfferToAssist> otas = offerToAssistService.searchOffersToAssist(request);
        return offerToAssistDtoBuilder().buildPage(otas);
    }

    private DtoBuilder offerToAssistDtoBuilder() {
        return new DtoBuilder()
                .add("id")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                .add("additionalNotes")
                .add("partner", partnerDto())
                .add("publicId")
                .add("reason")
                ;
    }

    private DtoBuilder partnerDto() {
        return new DtoBuilder()
                .add("abbreviation")
                .add("id")
                .add("publicId")
                .add("name")
                .add("websiteUrl")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                ;
    }
}
