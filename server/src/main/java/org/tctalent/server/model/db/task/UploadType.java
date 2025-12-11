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

package org.tctalent.server.model.db.task;

/**
 * Different types of uploadable document
 *
 * @author John Cameron
 */
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
    passport,
    policeCheck,
    policeCheckTrans,
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
    visa
}
