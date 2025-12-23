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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tctalent.server.repository.db.read.dao.CandidateReadDao;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.service.db.CandidateDtoService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateDtoServiceImpl implements CandidateDtoService {
    private final CandidateReadDao candidateReadDao;
    private final ObjectMapper jsonMapper;

    @Override
    public List<CandidateReadDto> findByIds(Collection<Long> ids) throws JsonProcessingException {

        return candidateReadDao.findByIds(ids);
//        //This should fetch everything from the database in a single access
//        final List<CandidateFlatDto> flatDtos = candidateReadDao.findByIds(ids);
//
//        //Decode the joined attributes
//        List<CandidateReadDto> dtos = new ArrayList<>();
//        for (CandidateFlatDto candidateFlatDto : flatDtos) {
//            CandidateReadDto dto = new CandidateReadDto();
//
//            //todo This could be done using a mapstruct mapper - constrained to all simple attributes
//            dto.setId(candidateFlatDto.getId());
//            dto.setCandidateNumber(candidateFlatDto.getCandidateNumber());
//            dto.setPublicId(candidateFlatDto.getPublicId());
//
//            //Decode the joined attributes
//            dto.setCandidateJobExperiences(
//                jsonMapper.readValue(candidateFlatDto.getCandidateJobExperiences(),
//                    new TypeReference<>() {})
//            );
//
//            //TODO JC Other joined attributes
//
//            dtos.add(dto);
//        }
//
//        return dtos;
    }
}
