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

package org.tctalent.server.service.db.impl;

import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.model.db.TravelDocForm;
import org.tctalent.server.repository.db.TravelDocFormRepository;
import org.tctalent.server.request.form.TravelDocFormData;
import org.tctalent.server.service.db.CandidateFormService;
import org.tctalent.server.service.db.TravelDocFormInstanceService;


@Service
@RequiredArgsConstructor
public class TravelDocFormInstanceServiceImpl implements TravelDocFormInstanceService {

    private final CandidateFormService candidateFormService;
    private final TravelDocFormRepository travelDocFormRepository;

    @Override
    public @NonNull TravelDocForm createOrUpdateTravelDocForm(
        @NonNull Candidate candidate, @NonNull TravelDocFormData request) {

        final CandidateFormInstanceKey key = computeTravelDocFormKey(candidate);
        //Check if form instance exists and retrieve if it does.
        TravelDocForm form = travelDocFormRepository.findById(key).orElse(null);
        if (form == null) {
            form = new TravelDocForm();
            form.setId(key);
            form.setCandidateForm(getTravelDocForm());
            form.setCandidate(candidate);
            form.setCreatedDate(OffsetDateTime.now());
        } else {
            form.setUpdatedDate(OffsetDateTime.now());
        }
        form.setFirstName(request.getFirstName());
        form.setLastName(request.getLastName());
        form.setBirthCountry(request.getBirthCountry());
        form.setGender(request.getGender());
        form.setDateOfBirth(request.getDateOfBirth());
        form.setPlaceOfBirth(request.getPlaceOfBirth());
        form.setTravelDocType(request.getTravelDocType());
        form.setTravelDocNumber(request.getTravelDocNumber());
        form.setTravelDocIssuedBy(request.getTravelDocIssuedBy());
        form.setTravelDocIssueDate(request.getTravelDocIssueDate());
        form.setTravelDocExpiryDate(request.getTravelDocExpiryDate());
        return travelDocFormRepository.save(form);
    }

    @Override
    @NonNull
    public Optional<TravelDocForm> getTravelDocForm(@NonNull Candidate candidate) {
        //Construct the instance key
        CandidateFormInstanceKey key = computeTravelDocFormKey(candidate);
        //Check if form instance exists and retrieve if it does.
        return travelDocFormRepository.findById(key);
    }

    private CandidateForm getTravelDocForm() {
        return candidateFormService.getByName("TravelDocForm");
    }

    private CandidateFormInstanceKey computeTravelDocFormKey(@NonNull Candidate candidate) {
        CandidateForm candidateForm = getTravelDocForm();
        return new CandidateFormInstanceKey(candidate.getId(), candidateForm.getId());
    }
}
