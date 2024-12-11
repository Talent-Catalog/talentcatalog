/*
 * Copyright (c) 2024 Talent Catalog.
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

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;

@Getter
@Setter
@Entity
@Table(name = "candidate_visa_check")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_visa_check_id_seq", allocationSize = 1)
public class CandidateVisaCheck extends CandidateVisaCheckBase {

    public void populateIntakeData(CandidateVisaCheckData data) {

        if (data.getVisaProtection() != null) {
            setProtection(data.getVisaProtection());
        }
        if (data.getVisaProtectionGrounds() != null) {
            setProtectionGrounds(data.getVisaProtectionGrounds());
        }
        if (data.getVisaEnglishThreshold() != null) {
            setEnglishThreshold(data.getVisaEnglishThreshold());
        }
        if (data.getVisaEnglishThresholdNotes() != null) {
            setEnglishThresholdNotes(data.getVisaEnglishThresholdNotes());
        }
        if (data.getVisaHealthAssessment() != null) {
            setHealthAssessment(data.getVisaHealthAssessment());
        }
        if (data.getVisaHealthAssessmentNotes() != null) {
            setHealthAssessmentNotes(data.getVisaHealthAssessmentNotes());
        }
        if (data.getVisaCharacterAssessment() != null) {
            setCharacterAssessment(data.getVisaCharacterAssessment());
        }
        if (data.getVisaCharacterAssessmentNotes() != null) {
            setCharacterAssessmentNotes(data.getVisaCharacterAssessmentNotes());
        }
        if (data.getVisaSecurityRisk() != null) {
            setSecurityRisk(data.getVisaSecurityRisk());
        }
        if (data.getVisaSecurityRiskNotes() != null) {
            setSecurityRiskNotes(data.getVisaSecurityRiskNotes());
        }
        if (data.getVisaOverallRisk() != null) {
            setOverallRisk(data.getVisaOverallRisk());
        }
        if (data.getVisaOverallRiskNotes() != null) {
            setOverallRiskNotes(data.getVisaOverallRiskNotes());
        }
        if (data.getVisaValidTravelDocs() != null) {
            setValidTravelDocs(data.getVisaValidTravelDocs());
        }
        if (data.getVisaValidTravelDocsNotes() != null) {
            setValidTravelDocsNotes(data.getVisaValidTravelDocsNotes());
        }
        if (data.getVisaPathwayAssessment() != null) {
            setPathwayAssessment(data.getVisaPathwayAssessment());
        }
        if (data.getVisaPathwayAssessmentNotes() != null) {
            setPathwayAssessmentNotes(data.getVisaPathwayAssessmentNotes());
        }
        if (data.getVisaDestinationFamily() != null) {
            setDestinationFamily(data.getVisaDestinationFamily());
        }
        if (data.getVisaDestinationFamilyLocation() != null) {
            setDestinationFamilyLocation(data.getVisaDestinationFamilyLocation());
        }
    }

}
