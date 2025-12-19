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

package org.tctalent.server.request.candidate;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.model.db.AvailImmediateReason;
import org.tctalent.server.model.db.DependantRelations;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.FamilyRelations;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.HasPassport;
import org.tctalent.server.model.db.IeltsStatus;
import org.tctalent.server.model.db.IntRecruitReason;
import org.tctalent.server.model.db.LeftHomeReason;
import org.tctalent.server.model.db.MaritalStatus;
import org.tctalent.server.model.db.NotRegisteredStatus;
import org.tctalent.server.model.db.Registration;
import org.tctalent.server.model.db.ResidenceStatus;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.VaccinationStatus;
import org.tctalent.server.model.db.WorkPermit;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnemployedOther;
import org.tctalent.server.model.db.YesNoUnsure;

/**
 * Has fields for all candidate intake fields.
 * <p/>
 * An instance of this class is received from the browser on each update.
 * Each update will come from a single intake component - comprising one or
 * a small number of fields. Just values for those fields will be populated
 * in the class. All other fields will be null.
 * <p/>
 * Null fields are ignored - non null fields update the database.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class CandidateIntakeDataUpdate {

    private YesNoUnsure arrestImprison;
    private String arrestImprisonNotes;

    private LocalDate asylumYear;
    private LocalDate availDate;
    private YesNo availImmediate;
    private String availImmediateJobOps;
    private AvailImmediateReason availImmediateReason;
    private String availImmediateNotes;

    //Corresponds to CandidateCitizenship fields
    private Long citizenId;
    private Long citizenNationalityId;
    private HasPassport citizenHasPassport;
    private LocalDate citizenPassportExp;
    private String citizenNotes;

    private YesNo canDrive;

    private YesNo conflict;
    private String conflictNotes;

    private YesNo covidVaccinated;
    private VaccinationStatus covidVaccinatedStatus;
    private LocalDate covidVaccinatedDate;
    private String covidVaccineName;
    private String covidVaccineNotes;

    private YesNoUnsure crimeConvict;
    private String crimeConvictNotes;

    private Long birthCountryId;

    //Corresponds to CandidateDependant fields
    private Long dependantId;
    private DependantRelations dependantRelation;
    private String dependantRelationOther;
    private LocalDate dependantDob;
    private Gender dependantGender;
    private String dependantName;
    private Registration dependantRegistered;
    private String dependantRegisteredNumber;
    private String dependantRegisteredNotes;
    private YesNo dependantHealthConcerns;
    private String dependantHealthNotes;

    //Corresponds to CandidateDestination fields
    private Long destinationId;
    private Long destinationCountryId;
    private YesNoUnsure destinationInterest;
    private FamilyRelations destinationFamily;
    private String destinationLocation;
    private String destinationNotes;

    //Corresponds to CandidateExam fields
    private Long examId;
    private Exam examType;
    private String otherExam;
    private String examScore;
    private Long examYear;
    private String examNotes;

    private YesNo destLimit;
    private String destLimitNotes;

    private DocumentStatus drivingLicense;
    private LocalDate drivingLicenseExp;
    private Long drivingLicenseCountryId;

    private YesNo familyMove;
    private String familyMoveNotes;

    private YesNo healthIssues;
    private String healthIssuesNotes;
    private String homeLocation;
    private String hostChallenges;
    private Long hostEntryYear;
    private String hostEntryYearNotes;
    private YesNo hostEntryLegally;
    private String hostEntryLegallyNotes;
    private List<IntRecruitReason> intRecruitReasons;
    private String intRecruitOther;
    private YesNoUnsure intRecruitRural;
    private String intRecruitRuralNotes;

    private String englishAssessment;
    private String englishAssessmentScoreIelts;
    private Long englishAssessmentScoreDet;
    private String frenchAssessment;
    private Long frenchAssessmentScoreNclc;
    private List<LeftHomeReason> leftHomeReasons;
    private String leftHomeNotes;
    private YesNo militaryService;
    private YesNoUnsure militaryWanted;
    private String militaryNotes;
    private LocalDate militaryStart;
    private LocalDate militaryEnd;

    private MaritalStatus maritalStatus;
    private String maritalStatusNotes;
    private YesNoUnsure partnerRegistered;
    private Long partnerCandId;
    private Long partnerEduLevelId;
    private String partnerEduLevelNotes;
    private Long partnerOccupationId;
    private String partnerOccupationNotes;
    private YesNo partnerEnglish;
    private Long partnerEnglishLevelId;
    private IeltsStatus partnerIelts;
    private String partnerIeltsScore;
    private Long partnerIeltsYr;
    private List<Long> partnerCitizenship;

    private ResidenceStatus residenceStatus;
    private String residenceStatusNotes;

    private YesNoUnsure returnedHome;
    private String returnedHomeReason;
    private String returnedHomeReasonNo;
    private YesNoUnsure returnHomeSafe;
    private YesNoUnsure returnHomeFuture;
    private String returnHomeWhen;

    private YesNo resettleThird;
    private String resettleThirdStatus;

    private YesNoUnsure unhcrRegistered;
    private UnhcrStatus unhcrStatus;
    private String unhcrNumber;
    private Long unhcrFile;
    private NotRegisteredStatus unhcrNotRegStatus;
    private YesNo unhcrConsent;
    private String unhcrNotes;
    private YesNoUnsure unrwaRegistered;
    private String unrwaNumber;
    private Long unrwaFile;
    private NotRegisteredStatus unrwaNotRegStatus;
    private String unrwaNotes;

    private YesNoUnsure visaReject;
    private String visaRejectNotes;
    private YesNoUnsure visaIssues;
    private String visaIssuesNotes;

    private YesNo workAbroad;
    private String workAbroadNotes;
    private WorkPermit workPermit;
    private YesNoUnsure workPermitDesired;
    private String workPermitDesiredNotes;
    private YesNoUnemployedOther workDesired;
    private String workDesiredNotes;

}
