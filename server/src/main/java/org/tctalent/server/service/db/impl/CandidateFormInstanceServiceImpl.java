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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.MyFirstForm;
import org.tctalent.server.model.db.RsdEvidenceForm;
import org.tctalent.server.model.db.mapper.CandidateMapper;
import org.tctalent.server.repository.db.MyFirstFormRepository;
import org.tctalent.server.repository.db.RsdEvidenceFormRepository;
import org.tctalent.server.request.form.MyFirstFormData;
import org.tctalent.server.request.form.RsdEvidenceFormData;
import org.tctalent.server.service.db.CandidateFormInstanceService;
import org.tctalent.server.service.db.CandidateFormService;
import org.tctalent.server.service.db.CandidatePropertyService;

@Service
@RequiredArgsConstructor
public class CandidateFormInstanceServiceImpl implements CandidateFormInstanceService {

    private final CandidateFormService candidateFormService;
    private final CandidateMapper candidateMapper;
    private final CandidatePropertyService candidatePropertyService;
    private final MyFirstFormRepository myFirstFormRepository;
    private final RsdEvidenceFormRepository rsdEvidenceFormRepository;

    @Override
    public @NonNull MyFirstForm createOrUpdateMyFirstForm(
        @NonNull Candidate candidate, @NonNull MyFirstFormData request) {

        final CandidateFormInstanceKey key = computeMyFirstFormKey(candidate);

        //Check if form instance exists and retrieve if it does.
        MyFirstForm form = myFirstFormRepository.findById(key).orElse(null);
        if (form == null) {
            form = new MyFirstForm();
            form.setId(key);
            form.setCandidateForm(getMyFirstForm());
            form.setCandidate(candidate);
            form.setCreatedDate(OffsetDateTime.now());
        } else {
            form.setUpdatedDate(OffsetDateTime.now());
        }

        form.setCity(request.getCity());
        form.setHairColour(request.getHairColour());

        return myFirstFormRepository.save(form);
    }

    @Override
    @NonNull
    public Optional<MyFirstForm> getMyFirstForm(@NonNull Candidate candidate) {
        //Construct the instance key
        CandidateFormInstanceKey key = computeMyFirstFormKey(candidate);

        //Check if form instance exists and retrieve if it does.
        return myFirstFormRepository.findById(key);
    }

    @Override
    public @NonNull RsdEvidenceForm createOrUpdateRsdEvidenceForm(
        @NonNull Candidate candidate, @NonNull RsdEvidenceFormData request) {

        final CandidateFormInstanceKey key = computeRsdEvidenceFormKey(candidate);
        RsdEvidenceForm form = rsdEvidenceFormRepository.findById(key).orElse(null);
        if (form == null) {
            form = new RsdEvidenceForm();
            form.setId(key);
            form.setCandidateForm(getRsdEvidenceForm());
            form.setCandidate(candidate);
            form.setCreatedDate(OffsetDateTime.now());
        } else {
            form.setUpdatedDate(OffsetDateTime.now());
        }

        form.setRefugeeStatus(request.getRefugeeStatus());
        form.setDocumentType(request.getDocumentType());
        form.setDocumentNumber(request.getDocumentNumber());

        return rsdEvidenceFormRepository.save(form);
    }

    @Override
    @NonNull
    public Optional<RsdEvidenceForm> getRsdEvidenceForm(@NonNull Candidate candidate) {
        CandidateFormInstanceKey key = computeRsdEvidenceFormKey(candidate);
        return rsdEvidenceFormRepository.findById(key);
    }

    @Override
    public void populateCandidateFromPending(
        @NonNull Candidate pendingCandidate, @NonNull Candidate candidate) {

            //This copies across non-null fields - except for Candidate Properties
            candidateMapper.updateCandidateFromSource(pendingCandidate, candidate);

            //Copy across properties
            //Note that we can't just simply transfer properties directly from one candidate
            //object to another because each property is associated with a particular candidate.
            //We have to use the CandidatePropertyService.
            Map<String, CandidateProperty> pendingProperties = pendingCandidate.getCandidateProperties();
            if (pendingProperties != null) {
                for (Entry<String, CandidateProperty> entry : pendingProperties.entrySet()) {
                    candidatePropertyService.createOrUpdateProperty(
                        candidate, entry.getKey(), entry.getValue().getValue(), null);
                }
            }
    }

    private CandidateForm getMyFirstForm() {
        return candidateFormService.getByName("MyFirstForm");
    }

    private CandidateFormInstanceKey computeMyFirstFormKey(@NonNull Candidate candidate) {
        CandidateForm candidateForm = getMyFirstForm();
        return new CandidateFormInstanceKey(candidate.getId(), candidateForm.getId());
    }

    private CandidateForm getRsdEvidenceForm() {
        return candidateFormService.getByName("RsdEvidenceForm");
    }

    private CandidateFormInstanceKey computeRsdEvidenceFormKey(@NonNull Candidate candidate) {
        CandidateForm candidateForm = getRsdEvidenceForm();
        return new CandidateFormInstanceKey(candidate.getId(), candidateForm.getId());
    }
}

