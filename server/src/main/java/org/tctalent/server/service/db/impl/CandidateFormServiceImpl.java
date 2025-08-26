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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.repository.db.CandidateFormRepository;
import org.tctalent.server.service.db.CandidateFormService;

@Service
@RequiredArgsConstructor
public class CandidateFormServiceImpl implements CandidateFormService {
    private final CandidateFormRepository candidateFormRepository;

    @NonNull
    @Override
    public CandidateForm get(long id) throws NoSuchObjectException {
        return candidateFormRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(CandidateForm.class, id));
    }

    @NonNull
    @Override
    public CandidateForm getByName(String name) throws NoSuchObjectException {
        return candidateFormRepository.findByName(name)
            .orElseThrow(() -> new NoSuchObjectException(CandidateForm.class, name));
    }
}
