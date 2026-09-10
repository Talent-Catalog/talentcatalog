/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.JobOpportunityStage;

class HelpLinkGeneratorFromOppStagesTest {

  /**
   * Not really a test - but just uncomment @Test annotation to run this to generate the HelpLink DB
   * Insert statements for all job and candidate opp stages.
   */
//    @Test
  void generateStandardStageBasedHelp() {
    HelpLinkGeneratorFromOppStages generator = new HelpLinkGeneratorFromOppStages();
    generator.generateHelpLinks();
  }


  @Test
  void generateHelpLinksPrintsInsertStatementsForAllJobAndCaseOpportunityStages() {
    HelpLinkGeneratorFromOppStages generator = new HelpLinkGeneratorFromOppStages();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;

    try {
      System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

      generator.generateHelpLinks();
    } finally {
      System.setOut(originalOut);
    }

    String output = out.toString(StandardCharsets.UTF_8);
    List<String> lines = output.lines().toList();

    int expectedLineCount =
        JobOpportunityStage.values().length + CandidateOpportunityStage.values().length;

    assertEquals(expectedLineCount, lines.size());

    assertTrue(lines.contains(
        "INSERT INTO help_link(label,job_stage,link) VALUES "
            + "('Briefing (Job)','briefing',"
            + "'https://tchelp.tettra.site/pages/job-opportunity-stages#briefing');"
    ));

    assertTrue(lines.contains(
        "INSERT INTO help_link(label,case_stage,link) VALUES "
            + "('Mini intake (Case)','miniIntake',"
            + "'https://tchelp.tettra.site/pages/candidate-opportunity-stages#mini-intake');"
    ));

    assertTrue(lines.contains(
        "INSERT INTO help_link(label,job_stage,link) VALUES "
            + "('Job offer (Job)','jobOffer',"
            + "'https://tchelp.tettra.site/pages/job-opportunity-stages#job-offer');"
    ));

    assertTrue(lines.contains(
        "INSERT INTO help_link(label,case_stage,link) VALUES "
            + "('Candidate was mistakenly proposed as a prospect for the job (Case)',"
            + "'candidateMistakenProspect',"
            + "'https://tchelp.tettra.site/pages/candidate-opportunity-stages"
            + "#candidate-was-mistakenly-proposed-as-a-prospect-for-the-job');"
    ));
  }
}
