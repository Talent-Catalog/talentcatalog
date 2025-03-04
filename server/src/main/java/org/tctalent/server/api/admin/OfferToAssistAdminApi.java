// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.anonymization.model.OfferToAssistCandidates201Response;
import org.tctalent.server.model.db.OfferToAssist;
import org.tctalent.server.request.OfferToAssistRequest;
import org.tctalent.server.service.db.OfferToAssistService;

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
}
