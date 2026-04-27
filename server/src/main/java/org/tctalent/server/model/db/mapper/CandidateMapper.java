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

package org.tctalent.server.model.db.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.tctalent.anonymization.model.CandidateRegistration;
import org.tctalent.server.model.db.Candidate;

/**
 * Candidate related mappings.
 * @author John Cameron
 */
@Mapper(uses = {
    CountryMapper.class,
    EducationLevelMapper.class,
    EducationMajorMapper.class,
    EnumsMapper.class,
    LanguageLevelMapper.class,
    LanguageMapper.class,
    OccupationMapper.class,
    PartnerMapper.class,
    SurveyTypeMapper.class
})
public interface CandidateMapper {

    //Take account of one changed field name
    @Mapping(target = "contactConsentPartners", source = "contactConsentTcPartners")
    Candidate candidateMapAllFields(CandidateRegistration registrationInfo);

    /**
     * Copies non null values in source to candidate
     * @param source Source candidate
     * @param candidate Target candidate
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidateProperties", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCandidateFromSource(Candidate source, @MappingTarget Candidate candidate);

}
