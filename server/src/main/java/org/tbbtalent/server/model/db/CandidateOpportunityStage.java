/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

/**
 * Note that the string values of this enum MUST match the actual stage names for candiadte
 * opportunities on Salesforce.
 * <p/>
 * See https://docs.google.com/document/d/1B6DmpYaONV_yNmyAqL76cu0TUQcpNgKtOmKELCkpRoc/edit#heading=h.qx7je1tuwoqv
 * <p/>
 */
public enum CandidateOpportunityStage {
        prospect ("Prospect"),
        miniIntake ("Mini intake"),
        fullIntake ("Full intake"),
        visaEligibility ("Visa eligibility"),
        cvPreparation ("CV preparation"),
        cvReview ("CV review"),
        oneWayPreparation ("1 way preparation"),
        oneWayReview ("1 way review"),
        testPreparation ("Test preparation"),
        testing ("Testing"),
        twoWayPreparation ("2 way preparation"),
        twoWayReview ("2 way review"),
        offer ("Offer"),
        acceptance ("Acceptance"),
        provincialVisaPreparation ("Provincial visa preparation"),
        provincialVisaProcessing ("Provincial visa processing"),
        visaPreparation ("Visa preparation"),
        visaProcessing ("Visa processing"),
        relocating ("Relocating"),
        relocated ("Relocated"),
        settled ("Settled"),
        durableSolution ("Durable solution"),
        noJobOffer ("No job offer"),
        noVisa ("No visa"),
        notFitForRole ("Not fit for role"),
        notEligibleForVisa ("Not eligible for visa"),
        noInterview ("No interview"),
        candidateRejectsOffer ("Candidate rejects offer"),
        candidateWithdraws ("Candidate withdraws")
        ;

        public final String label;

        CandidateOpportunityStage(String label) {
                this.label = label;
        }

        @Override
        public String toString() {
                return label;
        }

        public static CandidateOpportunityStage textToEnum(String label) {
                for (CandidateOpportunityStage stage : CandidateOpportunityStage.values()) {
                        if (stage.label.equals(label)) {
                                return stage;
                        }
                }
                throw new IllegalArgumentException(
                    "Unrecognized CandidateOpportunityStage: " + label);
        }

}
