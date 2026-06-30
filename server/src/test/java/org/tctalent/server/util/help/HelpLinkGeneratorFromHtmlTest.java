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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.JobOpportunityStage;

class HelpLinkGeneratorFromHtmlTest {

    private HelpLinkGeneratorFromHtml generator;

    @BeforeEach
    void setUp() {
        generator = new HelpLinkGeneratorFromHtml();
    }

    @Test
    void generateCanadaHelpFromHtmlParsesValidHeadingsAndSkipsInvalidOnes() throws Exception {
        String url = "https://tchelp.example/canada";
        Document document = Jsoup.parse("""
        <html>
          <body>
            <h3><a class="header-link" href="#empty"></a></h3>

            <h3>No link heading</h3>

            <h3 id="no-table">
              No table heading
              <a class="header-link" href="#no-table"></a>
            </h3>
            <p>This is not a table</p>

            <h3 id="no-description">
              No description heading
              <a class="header-link" href="#no-description"></a>
            </h3>
            <table>
              <tr>
                <td><p>SF Stage</p></td>
                <td><p>Candidate opp → <strong>Mini intake</strong></p></td>
              </tr>
            </table>

            <h3 id="no-stage">
              No stage heading
              <a class="header-link" href="#no-stage"></a>
            </h3>
            <table>
              <tr>
                <td><p>Description</p></td>
                <td><p>Description only</p></td>
              </tr>
            </table>

            <h3 id="valid-sf">
              Valid SF heading
              <a class="header-link" href="#valid-sf"></a>
            </h3>
            <table>
              <tr>
                <td><p>Description</p></td>
                <td><p>Valid description</p></td>
              </tr>
              <tr>
                <td><p>SF Stage</p></td>
                <td>
                  <p>Candidate opp → <strong>Mini intake</strong></p>
                  <p>Employer opp → <strong>Briefing</strong></p>
                </td>
              </tr>
            </table>

            <h3 id="valid-tc-sf">
              Valid TC/SF heading
              <a class="header-link" href="#valid-tc-sf"></a>
            </h3>
            <table>
              <tr>
                <td><p>Description</p></td>
                <td><p>Valid description</p></td>
              </tr>
              <tr>
                <td><p>TC/SF Stage</p></td>
                <td>
                  <p>Candidate opp → <strong>Full intake</strong></p>
                  <p>Employer opp → <strong>Pitching</strong></p>
                </td>
              </tr>
            </table>
          </body>
        </html>
        """);

        Connection connection = mock(Connection.class);
        when(connection.get()).thenReturn(document);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try (MockedStatic<Jsoup> jsoup = Mockito.mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(url)).thenReturn(connection);

            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

            generator.generateCanadaHelpFromHtml(url);
        } finally {
            System.setOut(originalOut);
        }

        String output = out.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Created 4 HelpLinks"));
        assertTrue(output.contains(
            "INSERT INTO help_link(country_id,label,job_stage,case_stage,link) VALUES "
                + "(6216,'Valid SF heading',null,'miniIntake','https://tchelp.example/canada#valid-sf');"
        ));
        assertTrue(output.contains(
            "INSERT INTO help_link(country_id,label,job_stage,case_stage,link) VALUES "
                + "(6216,'Valid SF heading','briefing',null,'https://tchelp.example/canada#valid-sf');"
        ));
        assertTrue(output.contains(
            "INSERT INTO help_link(country_id,label,job_stage,case_stage,link) VALUES "
                + "(6216,'Valid TC/SF heading',null,'fullIntake','https://tchelp.example/canada#valid-tc-sf');"
        ));
        assertTrue(output.contains(
            "INSERT INTO help_link(country_id,label,job_stage,case_stage,link) VALUES "
                + "(6216,'Valid TC/SF heading','pitching',null,'https://tchelp.example/canada#valid-tc-sf');"
        ));
    }

    @Test
    void extractStageNamesSeparatesCaseStagesAndJobStages() throws Exception {
        Element stageCell = stageCell("""
    <p>Candidate opp → <strong>Mini intake</strong><strong>Full intake</strong></p>
    <p>Employer opp → <strong>Briefing</strong><strong>Pitching</strong></p>
    """);

        List<String> caseStages = invokeStringList(
            new Class<?>[] {Element.class, boolean.class},
            stageCell,
            false
        );
        List<String> jobStages = invokeStringList(
            new Class<?>[] {Element.class, boolean.class},
            stageCell,
            true
        );

        assertEquals(List.of("Mini intake", "Full intake"), caseStages);
        assertEquals(List.of("Briefing", "Pitching"), jobStages);
    }

    @Test
    void extractCaseStagesConvertsValidStagesAndSkipsInvalidStages() throws Exception {
        Element stageCell = stageCell("""
    <p>Candidate opp → <strong>Mini intake</strong><strong>Bad case stage</strong></p>
    """);

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;

        List<CandidateOpportunityStage> result;
        try {
            System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));

            result = invokeCaseStageList(
                new Class<?>[] {Element.class},
                stageCell
            );
        } finally {
            System.setErr(originalErr);
        }

        assertEquals(List.of(CandidateOpportunityStage.miniIntake), result);
        assertTrue(err.toString(StandardCharsets.UTF_8)
            .contains("Could not decode case stage name: Bad case stage"));
    }

    @Test
    void extractJobStagesConvertsValidStagesAndSkipsInvalidStages() throws Exception {
        Element stageCell = stageCell("""
    <p>Employer opp → <strong>Briefing</strong><strong>Bad job stage</strong></p>
    """);

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;

        List<JobOpportunityStage> result;
        try {
            System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));

            result = invokeJobStageList(
                new Class<?>[] {Element.class},
                stageCell
            );
        } finally {
            System.setErr(originalErr);
        }

        assertEquals(List.of(JobOpportunityStage.briefing), result);
        assertTrue(err.toString(StandardCharsets.UTF_8)
            .contains("Could not decode job stage name: Bad job stage"));
    }

    @Test
    void createHelpLinkSetsCaseStageJobStageLabelLinkAndNextStepInfo() throws Exception {
        HelpLink helpLink = invokeHelpLink(
            new Class<?>[] {
                CandidateOpportunityStage.class,
                JobOpportunityStage.class,
                String.class,
                String.class
            },
            CandidateOpportunityStage.miniIntake,
            JobOpportunityStage.briefing,
            "Schedule call",
            "https://example.com#schedule-call"
        );

        assertEquals(CandidateOpportunityStage.miniIntake, helpLink.getCaseStage());
        assertEquals(JobOpportunityStage.briefing, helpLink.getJobStage());
        assertEquals("Schedule call", helpLink.getLabel());
        assertEquals("https://example.com#schedule-call", helpLink.getLink());
      assertNotNull(helpLink.getNextStepInfo());
    }

    @Test
    void generateDBInsertCommandHandlesNullCountryNullJobAndNullCaseStages() throws Exception {
        HelpLink caseHelpLink = new HelpLink();
        caseHelpLink.setLabel("Case label");
        caseHelpLink.setCaseStage(CandidateOpportunityStage.miniIntake);
        caseHelpLink.setLink("https://example.com#case");

        HelpLink jobHelpLink = new HelpLink();
        jobHelpLink.setLabel("Job label");
        jobHelpLink.setJobStage(JobOpportunityStage.briefing);
        jobHelpLink.setLink("https://example.com#job");

        String caseSql = invokeString(
            "generateDBInsertCommand",
            new Class<?>[] {Long.class, HelpLink.class},
            null,
            caseHelpLink
        );
        String jobSql = invokeString(
            "generateDBInsertCommand",
            new Class<?>[] {Long.class, HelpLink.class},
            6216L,
            jobHelpLink
        );

        assertEquals(
            "INSERT INTO help_link(country_id,label,job_stage,case_stage,link) VALUES "
                + "(null,'Case label',null,'miniIntake','https://example.com#case');",
            caseSql
        );
        assertEquals(
            "INSERT INTO help_link(country_id,label,job_stage,case_stage,link) VALUES "
                + "(6216,'Job label','briefing',null,'https://example.com#job');",
            jobSql
        );
    }

    @Test
    void getDescriptionRowValueReturnsDescriptionTextOrNull() throws Exception {
        Element tableWithDescription = Jsoup.parse("""
        <table>
          <tr>
            <td><p>Description</p></td>
            <td><p>This is the description.</p></td>
          </tr>
        </table>
        """).selectFirst("table");

        Element tableWithoutDescription = Jsoup.parse("""
        <table>
          <tr>
            <td><p>Responsible</p></td>
            <td><p>Team</p></td>
          </tr>
        </table>
        """).selectFirst("table");

        assertEquals(
            "This is the description.",
            invokeString("getDescriptionRowValue", new Class<?>[] {Element.class}, tableWithDescription)
        );
        assertNull(invokeString(
            "getDescriptionRowValue",
            new Class<?>[] {Element.class},
            tableWithoutDescription
        ));
    }

    @Test
    void getStageRowValuePrefersSfStageThenFallsBackToTcSfStageAndReturnsNullWhenMissing()
        throws Exception {
        Element sfTable = Jsoup.parse("""
        <table>
          <tr>
            <td><p>SF Stage</p></td>
            <td><p>Candidate opp → <strong>Mini intake</strong></p></td>
          </tr>
        </table>
        """).selectFirst("table");

        Element tcSfTable = Jsoup.parse("""
        <table>
          <tr>
            <td><p>TC/SF Stage</p></td>
            <td><p>Employer opp → <strong>Briefing</strong></p></td>
          </tr>
        </table>
        """).selectFirst("table");

        Element missingTable = Jsoup.parse("""
        <table>
          <tr>
            <td><p>Responsible</p></td>
            <td><p>Team</p></td>
          </tr>
        </table>
        """).selectFirst("table");

        assertEquals(
            "Candidate opp → Mini intake",
            Objects.requireNonNull(
                invokeElement("getStageRowValue", new Class<?>[]{Element.class}, sfTable)).text()
        );
        assertEquals(
            "Employer opp → Briefing",
            Objects.requireNonNull(
                invokeElement("getStageRowValue", new Class<?>[]{Element.class}, tcSfTable)).text()
        );
        assertNull(invokeElement("getStageRowValue", new Class<?>[] {Element.class}, missingTable));
    }

    @Test
    void getRowValueReturnsOnlyRowsWithTwoColumnsAndMatchingName() throws Exception {
        Element table = Jsoup.parse("""
        <table>
          <tr>
            <td><p>Description</p></td>
          </tr>
          <tr>
            <td><p>Responsible</p></td>
            <td><p>Team</p></td>
          </tr>
          <tr>
            <td><p>Description</p></td>
            <td><p>Expected description</p></td>
          </tr>
        </table>
        """).selectFirst("table");

        Element rowValue = invokeElement(
            "getRowValue",
            new Class<?>[] {String.class, Element.class},
            "Description",
            table
        );

      assert rowValue != null;
      assertEquals("Expected description", rowValue.text());
        assertNull(invokeElement(
            "getRowValue",
            new Class<?>[] {String.class, Element.class},
            "Missing",
            table
        ));
    }

    @Test
    void getHeadingTableReturnsImmediateTableOnly() throws Exception {
        Document withTable = Jsoup.parse("""
        <h3 id="with-table">Heading<a href="#with-table"></a></h3>
        <table></table>
        """);
        Document withParagraph = Jsoup.parse("""
        <h3 id="with-paragraph">Heading<a href="#with-paragraph"></a></h3>
        <p>Not table</p>
        """);
        Document withNoNextElement = Jsoup.parse("""
        <h3 id="no-next">Heading<a href="#no-next"></a></h3>
        """);

        Element table = invokeElement(
            "getHeadingTable",
            new Class<?>[] {Element.class},
            withTable.selectFirst("h3")
        );

        assertSame(withTable.selectFirst("table"), table);
        assertNull(invokeElement(
            "getHeadingTable",
            new Class<?>[] {Element.class},
            withParagraph.selectFirst("h3")
        ));
        assertNull(invokeElement(
            "getHeadingTable",
            new Class<?>[] {Element.class},
            withNoNextElement.selectFirst("h3")
        ));
    }

    @Test
    void getHeadingLinkReturnsHrefOnlyWhenExactlyOneAnchorExists() throws Exception {
        Element oneLink = Jsoup.parse("""
        <h3>Heading<a href="#one"></a></h3>
        """).selectFirst("h3");
        Element noLink = Jsoup.parse("""
        <h3>Heading</h3>
        """).selectFirst("h3");
        Element twoLinks = Jsoup.parse("""
        <h3>Heading<a href="#one"></a><a href="#two"></a></h3>
        """).selectFirst("h3");

        assertEquals(
            "#one",
            invokeString("getHeadingLink", new Class<?>[] {Element.class}, oneLink)
        );
        assertNull(invokeString("getHeadingLink", new Class<?>[] {Element.class}, noLink));
        assertNull(invokeString("getHeadingLink", new Class<?>[] {Element.class}, twoLinks));
    }

    private Object invoke(String methodName, Class<?>[] parameterTypes, Object... args)
        throws Exception {
        Method method = HelpLinkGeneratorFromHtml.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(generator, args);
    }

    private String invokeString(String methodName, Class<?>[] parameterTypes, Object... args)
        throws Exception {
        Object result = invoke(methodName, parameterTypes, args);
        return result == null ? null : (String) result;
    }

    private Element invokeElement(String methodName, Class<?>[] parameterTypes, Object... args)
        throws Exception {
        Object result = invoke(methodName, parameterTypes, args);
        return result == null ? null : (Element) result;
    }

    private HelpLink invokeHelpLink(Class<?>[] parameterTypes, Object... args)
        throws Exception {
        return (HelpLink) invoke("createHelpLink", parameterTypes, args);
    }

    private List<String> invokeStringList(Class<?>[] parameterTypes, Object... args)
        throws Exception {
        Object result = invoke("extractStageNames", parameterTypes, args);
      assertInstanceOf(List.class, result);

        return ((List<?>) result).stream()
            .map(String.class::cast)
            .toList();
    }

    private List<CandidateOpportunityStage> invokeCaseStageList(
        Class<?>[] parameterTypes,
        Object... args
    ) throws Exception {
        Object result = invoke("extractCaseStages", parameterTypes, args);
      assertInstanceOf(List.class, result);

        return ((List<?>) result).stream()
            .map(CandidateOpportunityStage.class::cast)
            .toList();
    }

    private List<JobOpportunityStage> invokeJobStageList(
        Class<?>[] parameterTypes,
        Object... args
    ) throws Exception {
        Object result = invoke("extractJobStages", parameterTypes, args);
      assertInstanceOf(List.class, result);

        return ((List<?>) result).stream()
            .map(JobOpportunityStage.class::cast)
            .toList();
    }
    private Element stageCell(String htmlInsideTd) {
        Element stageCell = Jsoup.parse("""
      <table>
        <tr>
          <td>
            %s
          </td>
        </tr>
      </table>
      """.formatted(htmlInsideTd)).selectFirst("td");

      assertNotNull(stageCell);
        return stageCell;
    }
}