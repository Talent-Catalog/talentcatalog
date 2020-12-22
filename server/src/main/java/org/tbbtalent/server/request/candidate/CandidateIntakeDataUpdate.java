/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tbbtalent.server.model.db.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

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

    private LocalDate asylumYear;
    private YesNoUnsure availImmediate;
    private AvailImmediateReason availImmediateReason;
    private String availImmediateNotes;

    //Corresponds to CandidateCitizenship fields
    private Long citizenId;
    private Long citizenNationalityId;
    private HasPassport citizenHasPassport;
    private LocalDate citizenPassportExp;
    private String citizenNotes;

    private YesNo canDrive;

    private YesNo children;
    private String childrenAge;

    private YesNo conflict;
    private String conflictNotes;

    private YesNoUnsure crimeConvict;
    private String crimeConvictNotes;

    private Long dependants;
    private String dependantsNotes;

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

    private YesNo destLimit;
    private String destLimitNotes;

    private YesNo destJob;
    private String destJobNotes;

    private DrivingLicenseStatus drivingLicense;
    private LocalDate drivingLicenseExp;
    private Long drivingLicenseCountryId;

    private YesNo familyMove;
    private String familyMoveNotes;
    private YesNo familyHealthConcern;
    private String familyHealthConcernNotes;
    private String homeLocation;
    private String hostChallenges;
    private YesNo hostBorn;
    private Long hostEntryYear;
    private YesNo hostEntryLegally;
    private List<IntRecruitReason> intRecruitReasons;
    private YesNoUnsure intRecruitRural;

    private LeftHomeReason leftHomeReason;
    private String leftHomeOther;
    private YesNo militaryService;

    private MaritalStatus maritalStatus;
    private YesNoUnsure partnerRegistered;
    private Long partnerCandId;
    private Long partnerEduLevelId;
    private Long partnerProfessionId;
    private YesNo partnerEnglish;
    private Long partnerEnglishLevelId;
    private YesNoUnsure partnerIelts;
    private IeltsScore partnerIeltsScore;
    private Long partnerCitizenshipId;

    private ResidenceStatus residenceStatus;

    private YesNoUnsure returnedHome;
    private String returnedHomeNotes;
    private String returnedHomeReason;
    private YesNoUnsure returnHomeSafe;
    private YesNoUnsure returnHomeFuture;
    private String returnHomeWhen;

    private YesNo resettleThird;
    private String resettleThirdStatus;

    private UnhcrStatus unhcrStatus;
    private UnhcrStatus unhcrOldStatus;
    private String unhcrNumber;
    private Long unhcrFile;
    private String unhcrNotes;
    private YesNo unhcrPermission;
    private UnrwaStatus unrwaStatus;
    private String unrwaNumber;
    private String unrwaNotes;

    //Corresponds to CandidateVisaCheck fields
    private Long visaId;
    private String visaAssessmentNotes;
    private Long visaCountryId;
    private VisaEligibility visaEligibility;
    private OffsetDateTime visaCreatedDate;
    private Long visaCreatedById;
    private YesNo visaProtection;
    private String visaProtectionGrounds;
    private TBBEligibilityAssessment visaTbbEligibilityAssessment;

    private YesNoUnsure visaReject;
    private List<VisaIssue> visaIssues;
    private String visaIssuesNotes;
    
    private YesNo workAbroad;
    private Long workAbroadLocId;
    private Long workAbroadYrs;
    private WorkPermit workPermit;
    private YesNoUnsure workPermitDesired;
    private YesNo workLegally;

}
