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
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.model.db.MyFirstForm;
import org.tctalent.server.repository.db.CandidateFormRepository;
import org.tctalent.server.repository.db.MyFirstFormRepository;
import org.tctalent.server.request.form.MyFirstFormData;
import org.tctalent.server.service.db.CandidateFormInstanceService;

@Service
@RequiredArgsConstructor
public class CandidateFormInstanceServiceImpl implements CandidateFormInstanceService {

    private final CandidateFormRepository candidateFormRepository;
    private final MyFirstFormRepository myFirstFormRepository;

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

    private CandidateForm getMyFirstForm() {
        return candidateFormRepository.findByName("MyFirstForm")
            .orElseThrow(() -> new NoSuchObjectException(CandidateForm.class, "MyFirstForm"));
    }

    private CandidateFormInstanceKey computeMyFirstFormKey(@NonNull Candidate candidate) {
        CandidateForm candidateForm = getMyFirstForm();
        return new CandidateFormInstanceKey(candidate.getId(), candidateForm.getId());
    }
}
