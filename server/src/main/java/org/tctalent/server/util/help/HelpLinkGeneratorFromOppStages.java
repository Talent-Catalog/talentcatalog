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

package org.tctalent.server.util.help;

import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.JobOpportunityStage;

/**
 * Generates HelpLinks (as database insert commands) from our opportunity stages
 *
 * @author John Cameron
 */
public class HelpLinkGeneratorFromOppStages {

    /**
     * Sends database insert commands for help links to standard output.
     */
    public void generateHelpLinks() {
        final JobOpportunityStage[] jobValues = JobOpportunityStage.values();
        for (JobOpportunityStage value : jobValues) {
            String s = generateJobInsertStatement(value);
            System.out.println(s);
        }

        final CandidateOpportunityStage[] caseValues = CandidateOpportunityStage.values();
        for (CandidateOpportunityStage value : caseValues) {
            String s = generateCaseInsertStatement(value);
            System.out.println(s);
        }
    }

    private String generateCaseInsertStatement(CandidateOpportunityStage value) {
        String name = value.name();
        String sfName = value.getSalesforceStageName();
        return generateInsertStatement(name, sfName, false);
    }

    private String generateJobInsertStatement(JobOpportunityStage value) {
        String name = value.name();
        String sfName = value.getSalesforceStageName();
        return generateInsertStatement(name, sfName, true);
    }

    private String generateInsertStatement(String name, String sfName, boolean job) {

        String sb = "INSERT INTO help_link(label,"
            + (job ? "job_stage" : "case_stage")
            + ",link) VALUES ("
            + "'"
            + sfName
            + " ("
            + (job ? "Job" : "Case")
            + ")','"
            + name
            + "','"
            + "https://tchelp.tettra.site/pages/"
            + (job ? "job" : "candidate")
            + "-opportunity-stages"
            + "#"
            + convertStageNameToLinkAnchor(sfName)
            + "');";
        return sb;
    }

    private String convertStageNameToLinkAnchor(String sfName) {
        String s = sfName.toLowerCase();
        return s.replace(' ', '-');
    }
}
