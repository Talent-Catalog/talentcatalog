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

package org.tctalent.server.model.db;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.tctalent.anonymization.model.CandidateRegistration;

/**
 * Candidate related mappings.
 *
 * @author John Cameron
 */
@Mapper
public interface CandidateMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "city")
    @Mapping(target = "phone")
    @Mapping(target = "whatsapp")
    @Mapping(target = "gender")
    @Mapping(target = "contactConsentRegistration")
    @Mapping(target = "contactConsentPartners", source = "contactConsentTcPartners")
    @Mapping(target = "candidateCertifications")
    @Mapping(target = "candidateOccupations")
    Candidate candidateRegistrationToCandidate(CandidateRegistration registrationInfo);

    CandidateOccupation mapOccupation(org.tctalent.anonymization.model.CandidateOccupation publicOccupation);

    @ValueMapping(target="male", source = "MALE")
    @ValueMapping(target="female", source = "FEMALE")
    @ValueMapping(target="other", source = "OTHER")
    Gender publicGenderToInternalGender(org.tctalent.anonymization.model.Gender publicGender);

    @ValueMapping(target="active", source = "ACTIVE")
    @ValueMapping(target="inactive", source = "INACTIVE")
    @ValueMapping(target="deleted", source = "DELETED")
    Status publicStatusToInternalStatus(org.tctalent.anonymization.model.Status publicStatus);
}
