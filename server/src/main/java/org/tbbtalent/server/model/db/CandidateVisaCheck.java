/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "candidate_visa_check")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_visa_check_id_seq", allocationSize = 1)
public class CandidateVisaCheck extends CandidateVisaCheckBase {

    public void populateIntakeData(
            @NonNull Candidate candidate, @NonNull Country country,
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        setCountry(country);

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
    }
    
}
