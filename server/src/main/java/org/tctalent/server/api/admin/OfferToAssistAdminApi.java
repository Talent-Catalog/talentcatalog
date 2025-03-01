// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.anonymization.model.OfferToAssistCandidates201Response;
import org.tctalent.anonymization.model.OfferToAssistCandidatesRequest;

/**
 * TODO JC Maybe this could just be a redirect from tc-api
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/ota")
@Slf4j
@RequiredArgsConstructor
public class OfferToAssistAdminApi {

    @PostMapping
    @NonNull
    public ResponseEntity<OfferToAssistCandidates201Response>
        create(OfferToAssistCandidatesRequest request) {
        //TODO JC Create entity through service
        //TODO JC Return a public id
        return null;
    }
}
