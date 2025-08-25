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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.model.db.MyFirstForm;
import org.tctalent.server.repository.db.CandidateFormInstanceRepository;
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
    private final CandidateFormInstanceRepository candidateFormInstanceRepository;

    @Override
    public MyFirstForm createMyFirstForm(long candidateId, MyFirstFormUpdateRequest request) {
        MyFirstForm form = new MyFirstForm();

        //TODO JC Debug - always assume form 1
        CandidateForm candidateForm = candidateFormService.get(1L);
        form.setCandidateForm(candidateForm);

        ////TODO JC Maybe Candidate should be passed in
        Candidate candidate = candidateService.getCandidate(candidateId);
        form.setCandidate(candidate);

        form.setCity(request.getCity());

        //TODO JC Do I need to call candidateRepository.save and candidatePropertyRepository.save
        //TODO JC Look at CascadeAll in ChatGPT

        form.setHairColour(request.getHairColour());

        //TODO JC Need to construct id. Is there a better place to do this? What if it exists?
        form.setId(new CandidateFormInstanceKey(candidateId, candidateForm.getId()));

        return candidateFormInstanceRepository.save(form);
    }
}
