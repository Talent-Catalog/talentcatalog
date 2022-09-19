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
 * Note that the string values of this enum MUST match the actual stage names for candidate
 * opportunities on Salesforce.
 * <p/>
 * See https://docs.google.com/document/d/1B6DmpYaONV_yNmyAqL76cu0TUQcpNgKtOmKELCkpRoc/edit#heading=h.qx7je1tuwoqv
 * <p/>
 */
public enum CandidateOpportunityStage {
        prospect ("Prospect", false),
        miniIntake ("Mini intake", false),
        fullIntake ("Full intake", false),
        visaEligibility ("Visa eligibility", false),
        cvPreparation ("CV preparation", false),
        cvReview ("CV review", false),
        oneWayPreparation ("1 way preparation", false),
        oneWayReview ("1 way review", false),
        testPreparation ("Test preparation", false),
        testing ("Testing", false),
        twoWayPreparation ("2 way preparation", false),
        twoWayReview ("2 way review", false),
        offer ("Offer", false),
        acceptance ("Acceptance", false),
        provincialVisaPreparation ("Provincial visa preparation", true),
        provincialVisaProcessing ("Provincial visa processing", true),
        visaPreparation ("Visa preparation", true),
        visaProcessing ("Visa processing", true),
        relocating ("Relocating", true),
        relocated ("Relocated", true),
        settled ("Settled", true),
        durableSolution ("Durable solution", true),
        noJobOffer ("No job offer", false),
        noVisa ("No visa", false),
        notFitForRole ("Not fit for role", false),
        notEligibleForTC ("Not eligible for TC", false),
        notEligibleForVisa ("Not eligible for visa", false),
        noInterview ("No interview", false),
        candidateLeavesDestination ("Candidate leaves destination", true),
        candidateRejectsOffer ("Candidate rejects offer", false),
        candidateWithdraws ("Candidate withdraws", false)
        ;

        private final String salesforceStageName;

        private final boolean employed;

        /**
         * Initializes each enum value
         * @param salesforceStageName Name of stage on Salesforce
         * @param employed True if a candidate in this stage should be considered as employed and
         *                 therefore no longer available for other job opportunities.
         */
        CandidateOpportunityStage(String salesforceStageName, boolean employed) {
                this.salesforceStageName = salesforceStageName;
                this.employed = employed;
        }

        public boolean isEmployed() {
                return employed;
        }

        public String getSalesforceStageName() {
                return salesforceStageName;
        }

        @Override
        public String toString() {
                return salesforceStageName;
        }

        public static CandidateOpportunityStage textToEnum(String salesforceStageName) {
                for (CandidateOpportunityStage stage : CandidateOpportunityStage.values()) {
                        if (stage.salesforceStageName.equals(salesforceStageName)) {
                                return stage;
                        }
                }
                throw new IllegalArgumentException(
                    "Unrecognized CandidateOpportunityStage: " + salesforceStageName);
        }

}
