/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request.candidate.visa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tbbtalent.server.model.db.DocumentStatus;
import org.tbbtalent.server.model.db.OtherVisas;
import org.tbbtalent.server.model.db.RiskLevel;
import org.tbbtalent.server.model.db.TBBEligibilityAssessment;
import org.tbbtalent.server.model.db.VisaEligibility;
import org.tbbtalent.server.model.db.YesNo;
import org.tbbtalent.server.model.db.YesNoUnsure;

/**
 * TODO CC doc
 *
 * @author Caroline Cameron
 */
@Getter
@Setter
@ToString
public class CandidateVisaCheckData {
    private Long visaCountryId;
    private YesNo visaProtection;
    private String visaProtectionGrounds;
    private YesNo visaEnglishThreshold;
    private String visaEnglishThresholdNotes;
    private YesNo visaHealthAssessment;
    private String visaHealthAssessmentNotes;
    private YesNo visaCharacterAssessment;
    private String visaCharacterAssessmentNotes;
    private YesNo visaSecurityRisk;
    private String visaSecurityRiskNotes;
    private RiskLevel visaOverallRisk;
    private String visaOverallRiskNotes;
    private DocumentStatus visaValidTravelDocs;
    private String visaValidTravelDocsNotes;
    private String visaCreatedById;
    private YesNoUnsure visaPathwayAssessment;
    private String visaPathwayAssessmentNotes;

    //Corresponds to CandidateVisaJobCheck fields
    private Long visaJobId;
    private Long visaJobOccupationId;
    private String visaJobOccupationNotes;
    private YesNo visaJobQualification;
    private String visaJobQualificationNotes;
    private YesNo visaJobInterest;
    private String visaJobInterestNotes;
    private YesNo visaJobSalaryTsmit;
    private YesNo visaJobRegional;
    private YesNo visaJobFamilyAus;
    private YesNo visaJobEligible494;
    private String visaJobEligible494Notes;
    private YesNo visaJobEligible186;
    private String visaJobEligible186Notes;
    private OtherVisas visaJobEligibleOther;
    private String visaJobEligibleOtherNotes;
    private VisaEligibility visaJobPutForward;
    private TBBEligibilityAssessment visaJobTbbEligibility;
    private String visaJobNotes;
    private String visaJobRelevantWorkExp;
    private String visaJobAgeRequirement;
    private String visaJobPreferredPathways;
    private String visaJobIneligiblePathways;
    private String visaJobEligiblePathways;
    private String visaJobOccupationCategory;
    private String visaJobOccupationSubCategory;
    private YesNo visaJobEnglishThreshold;
    private String visaJobEnglishThresholdNotes;
}
