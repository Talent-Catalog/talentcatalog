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
        identifyingRoles ("Identifying roles"),
        candidateSearch ("Candidate search"),
        visaEligibility ("Visa eligibility"),
        cvPreparation ("CV preparation"),
        cvReview ("CV review"),
        recruitmentProcess ("Recruitment process"),
        jobOffer ("Job offer"),
        visaPreparation ("Visa preparation"),
        postHireEngagement ("Post hire engagement"),
        hiringCompleted ("Hiring completed"),
        employerIneligible ("Employer ineligible"),
        ineligibleOccupation ("Ineligible occupation"),
        ineligibleRegion ("Ineligible region"),
        noInterest ("No interest"),
        noJobOffer ("No job offer"),
        noPrPathway ("No PR pathway"),
        noSuitableCandidates ("No suitable candidates"),
        noVisa ("No visa"),
        tooExpensive ("Too expensive"),
        tooHighWage ("Too high wage"),
        tooLong ("Too long");

        public final String label;

        JobOpportunityStage(String label) {
                this.label = label;
        }

        @Override
        public String toString() {
                return label;
        }

        public static JobOpportunityStage textToEnum(String label) {
                for (JobOpportunityStage stage : JobOpportunityStage.values()) {
                        if (stage.label.equals(label)) {
                                return stage;
                        }
                }
                throw new IllegalArgumentException("Unrecognized JobOpportunityStage: " + label);
        }

}
