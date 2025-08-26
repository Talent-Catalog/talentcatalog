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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.model.db.MyFirstForm;
import org.tctalent.server.repository.db.MyFirstFormRepository;
import org.tctalent.server.request.form.MyFirstFormUpdateRequest;
import org.tctalent.server.service.db.CandidateFormInstanceService;
import org.tctalent.server.service.db.CandidateFormService;
import org.tctalent.server.service.db.CandidateService;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
@RequiredArgsConstructor
public class CandidateFormInstanceServiceImpl implements CandidateFormInstanceService {

    private final CandidateService candidateService;
    private final CandidateFormService candidateFormService;
    private final MyFirstFormRepository myFirstFormRepository;

    @Override
    public MyFirstForm createMyFirstForm(long candidateId, MyFirstFormUpdateRequest request) {
        //Get form template
        CandidateForm candidateForm = candidateFormService.getByName("MyFirstForm");
        ////TODO JC Maybe Candidate should be passed in
        Candidate candidate = candidateService.getCandidate(candidateId);

        CandidateFormInstanceKey key = new CandidateFormInstanceKey(candidateId, candidateForm.getId());

        //Check if form instance exists and retrieve if it does.
        MyFirstForm form = myFirstFormRepository.findById(key).orElse(null);
        if (form == null) {
            form = new MyFirstForm();
            form.setId(key);
            form.setCandidateForm(candidateForm);
            form.setCandidate(candidate);
            form.setCreatedDate(OffsetDateTime.now());
        } else {
            form.setUpdatedDate(OffsetDateTime.now());
        }

        form.setCity(request.getCity());
        form.setHairColour(request.getHairColour());

        return myFirstFormRepository.save(form);
    }
}
