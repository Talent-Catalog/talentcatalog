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

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.repository.db.read.dao.CandidateReadDao;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.service.db.CandidateDtoService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateDtoServiceImpl implements CandidateDtoService {
    private final CandidateReadDao candidateReadDao;

    @Override
    @NonNull
    public Map<Long, CandidateReadDto> loadByIds(Collection<Long> ids) throws JsonProcessingException {
        Map<Long, CandidateReadDto> result = new HashMap<>();
        //TODO JC populate result
        return result;
//        return candidateReadDao.findByIds(ids);
    }
}
