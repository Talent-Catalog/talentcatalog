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

/**
 * Note that the string values of this enum MUST match the actual stage names for candidate
 * opportunities on Salesforce.
 * See https://docs.google.com/document/d/1B6DmpYaONV_yNmyAqL76cu0TUQcpNgKtOmKELCkpRoc/edit#heading=h.qx7je1tuwoqv
 *
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
        training("Training", false, true, false),
        provincialVisaPreparation ("Provincial visa preparation", false, true, false),
        provincialVisaProcessing ("Provincial visa processing", false, true, false),
        visaPreparation ("Visa preparation", false, true, false),
        visaProcessing ("Visa processing", false, true, false),
        relocating ("Relocating", false, true, false),
        relocated ("Relocated", false, true, false),
        settled ("Settled", false, true, false),
        durableSolution ("Durable solution", true, true, true),
        noJobOffer ("No job offer", true, false, false),
        noVisa ("No visa", true, false, false),
        notFitForRole ("Not fit for role", true, false, false),
        notEligibleForTC ("Not eligible for TC", true, false, false),
        notEligibleForVisa ("Not eligible for visa", true, false, false),
        noInterview ("No interview", true, false, false),
        candidateLeavesDestination ("Candidate leaves destination", true, true, false),
        candidateMistakenProspect("Candidate was mistakenly proposed as a prospect for the job", true, false, false),
        candidateRejectsOffer ("Candidate rejects offer", true, false, false),
        candidateUnreachable ("Candidate unreachable", true, false, false),
        candidateWithdraws ("Candidate withdraws", true, false, false),
        jobIneligible("Job ineligible", true, false, false),
        jobOfferRetracted ("Job offer retracted", true, false, false),
        jobWithdrawn("Job withdrawn", true, false, false),
        relocatedNoJobOfferPathway("Relocated no job offer pathway", true, false, false),
        trainingNotCompleted("Training not completed", true, false, false),
        programSuspended("Program suspended", true, false, false),
        ;

        private final String salesforceStageName;

        private final boolean closed;
        private final boolean employed;
        private final boolean won;

        /**
         * Initializes each enum value
         * @param salesforceStageName Name of stage on Salesforce
         * @param closed if this is a closed stage
         * @param employed True if a candidate in this stage should be considered as employed and
         *                 therefore no longer available for other job opportunities.
         * @param won if this means the opportunity is won
         */
        CandidateOpportunityStage(String salesforceStageName, boolean closed, boolean employed, boolean won) {
                this.salesforceStageName = salesforceStageName;
                this.closed = closed;
                this.employed = employed;
                this.won = won;
        }

        CandidateOpportunityStage(String salesforceStageName) {
                this(salesforceStageName, false, false, false);
        }

        public boolean isClosed() { return closed; }

        public boolean isEmployed() {
                return employed;
        }

        public boolean isWon() { return won; }

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
