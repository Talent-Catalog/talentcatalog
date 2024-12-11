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
 * Note that the string values of this enum MUST match the actual stage names for job
 * opportunities on Salesforce.
 * <p/>
 * See https://docs.google.com/document/d/1B6DmpYaONV_yNmyAqL76cu0TUQcpNgKtOmKELCkpRoc/edit#heading=h.qx7je1tuwoqv
 * <p/>
 * MODEL - Enum's with String labels. Looking up enum from label.
 */
public enum JobOpportunityStage {
        prospect ("Prospect"),
        briefing ("Briefing"),
        pitching ("Pitching"),
        mou("MOU"),
        identifyingRoles ("Identifying roles"),
        candidateSearch ("Candidate search"),
        visaEligibility ("Visa eligibility"),
        cvPreparation ("CV preparation"),
        cvReview ("CV review"),
        recruitmentProcess ("Recruitment process"),
        jobOffer ("Job offer"),
        training("Training"),
        visaPreparation ("Visa preparation"),
        postHireEngagement ("Post hire engagement"),
        hiringCompleted ("Hiring completed", true, true),
        ineligibleEmployer ("Ineligible employer", true, false),
        ineligibleOccupation ("Ineligible occupation", true, false),
        ineligibleRegion ("Ineligible region", true, false),
        noInterest ("No interest", true, false),
        noJobOffer ("No job offer", true, false),
        noPrPathway ("No PR pathway", true, false),
        noSuitableCandidates ("No suitable candidates", true, false),
        noVisa ("No visa", true, false),
        tooExpensive ("Too expensive", true, false),
        tooHighWage ("Too high wage", true, false),
        tooLong ("Too long", true, false),
        mouIssue("MOU issue", true, false),
        trainingNotCompleted("Training not completed", true, false);

        private final String salesforceStageName;
        private final boolean closed;
        private final boolean won;


        JobOpportunityStage(String salesforceStageName, boolean closed, boolean won) {
                this.salesforceStageName = salesforceStageName;
                this.closed = closed;
                this.won = won;
        }

        JobOpportunityStage(String salesforceStageName) {
                this(salesforceStageName, false, false);
        }

        @Override
        public String toString() {
                return salesforceStageName;
        }

        public boolean isClosed() { return closed; }
        public boolean isWon() { return won; }

        public String getSalesforceStageName() {
                return salesforceStageName;
        }

        public static JobOpportunityStage textToEnum(String label) {
                for (JobOpportunityStage stage : JobOpportunityStage.values()) {
                        if (stage.salesforceStageName.equals(label)) {
                                return stage;
                        }
                }
                throw new IllegalArgumentException("Unrecognized JobOpportunityStage: " + label);
        }

}
