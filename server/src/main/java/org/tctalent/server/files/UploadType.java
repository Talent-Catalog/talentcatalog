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

package org.tctalent.server.files;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Different types of uploadable document
 *
 * @author John Cameron
 */
@Getter
@RequiredArgsConstructor
public enum UploadType {
    apcInterestForm,
    collaborationAgreement,
    conductEmployer,
    conductEmployerTrans,
    conductMinistry,
    conductMinistryTrans,
    cos,
    cv,
    degree,
    degreeTranscript,
    degreeTranscriptTrans,
    dependantRefugeeStatusDoc,
    dependantTravelDoc,
    englishExam,
    idCard,
    infoReleaseForm,
    licencing,
    licencingTrans,
    OETPulseResults,
    offer,
    otherId,
    otherIdTrans,
    passport(true),
    policeCheck(true),
    policeCheckTrans(true),
    proofAddress,
    proofAddressTrans,
    references,
    refugeeStatusDoc,
    residenceAttest,
    residenceAttestTrans,
    studiedInEnglish,
    travelDoc,
    other,
    unhcrUnrwaRegCard,
    vaccination,
    vaccinationTrans,
    visa;

    private final boolean signedAccess;

    UploadType() {
        this(false);
    }

}
