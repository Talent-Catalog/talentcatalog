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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.NextStepInfo;

/**
 * Utility for generating HelpLinks (as database insert commands) from HTML
 *
 * @author John Cameron
 */
public class HelpLinkGeneratorFromHtml {

    /**
     * Generate HelpLinks from Canada's Operations Manual.
     * <p/>
     * Canada help has the following form - see example below
     * <ul>
     *     <li>
     *         h3 header with name of step
     *     </li>
     *     <li>
     *         The id of the above header is used to construct the anchor in the following
     *         "a" element. That has the link within the document that will be used for the
     *         HelpLink link
     *     </li>
     *     <li>
     *         A table consisting of a standard set of rows as follows:
     *     </li>
     *     <li>
     *         Description row
     *     </li>
     *     <li>
     *         Stage(s) row - job or case opp stages - or both.
     *     <li>
     *         Responsible row - not used in HelpLink
     *     </li>
     *     <li>
     *         Resource row - not used in HelpLink
     *     </li>
     *     <li>
     *         Service standard row - a bit unstructured but could be used to manually set
     *         HelpLink nextStepDays
     *     </li>
     * </ul>
     * @param url URL of website version of Operations Manual
     * @throws IOException if there is a problem connecting to the l
     */
    /* Example
            <h3 id="schedule-call-with-employer">
              Schedule call with employer
              <a class="header-link" href="#schedule-call-with-employer"></a>
            </h3>
            <table>
              <tbody>
              <tr>
                <td><p>Description</p></td>
                <td><p>Email to schedule a call. Best practice is to use Salesforce email feature
                  and/or Calendly links.</p></td>
              </tr>
              <tr>
                <td><p>SF Stage</p></td>
                <td><p>Employer opp → <strong>Briefing</strong></p></td>
              </tr>
              <tr>
                <td><p>Responsible</p></td>
                <td><p>Employer Engagement Leadership Team, supported by Destination Recruitment
                  Team where requested</p></td>
              </tr>
              <tr>
                <td><p>Resource</p></td>
                <td><p>Email templates: <a
                    href="https://docs.google.com/document/d/1qkjeY_7_8Msqj2SfYsfgac5C2t4XQVOCiEyDzfCIz-4/edit"><u>LINK</u></a>
                </p></td>
              </tr>
              <tr>
                <td><p>Service Standard&nbsp;</p></td>
                <td><p>24-48 hours from receiving the employer lead referral.</p></td>
              </tr>
              </tbody>
            </table>
     */
    public void generateCanadaHelpFromHtml(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        //Pick up all the headings
        Elements headings = doc.select("h3");

        List<HelpLink> helpLinks = new ArrayList<>();
        for (Element heading : headings) {
            String headingText = heading.text().trim();

            //Ignore empty h3 headers
            if (!headingText.isEmpty()) {
                //Ignore headers without a link
                String headingLink = getHeadingLink(heading);
                if (headingLink != null) {
                    //Ignore headers without a following table
                    Element headingTable = getHeadingTable(heading);
                    if (headingTable != null) {
                        //Ignore heading without a Description row in the heading table
                        String description = getDescriptionRowValue(headingTable);
                        if (description != null) {
                            //Ignore heading without a stage row in the heading table
                            Element stageTextElement = getStageRowValue(headingTable);
                            if (stageTextElement != null) {
                                //Decode stage text - may need to generate more than one help link
                                List<CandidateOpportunityStage> caseStages = extractCaseStages(stageTextElement);
                                for (CandidateOpportunityStage caseStage : caseStages) {
                                    HelpLink helpLink = createHelpLink(
                                        caseStage, null, headingText, url+headingLink);
                                    helpLinks.add(helpLink);
                                }

                                List<JobOpportunityStage> jobStages = extractJobStages(stageTextElement);
                                for (JobOpportunityStage jobStage : jobStages) {
                                    HelpLink helpLink = createHelpLink(
                                        null, jobStage, headingText, url+headingLink);
                                    helpLinks.add(helpLink);
                                }

                            }
                        }
                    }
                }
            }
        }

        System.out.println("Created " + helpLinks.size() + " HelpLinks");

        long CANADA_COUNTRY_ID = 6216;
        for (HelpLink helpLink : helpLinks) {
            System.out.println(generateDBInsertCommand(CANADA_COUNTRY_ID, helpLink));
        }
    }

    private String generateDBInsertCommand(Long countryId, HelpLink helpLink) {
        String s = "INSERT INTO help_link(country_id,label,job_stage,case_stage,link) VALUES ("
            + (countryId == null ? "null" : countryId)
            + ",'"
            + helpLink.getLabel()
            + "',"
            + (helpLink.getJobStage() == null ? null : "'" + helpLink.getJobStage().name() + "'")
            + ","
            + (helpLink.getCaseStage() == null ? null : "'" + helpLink.getCaseStage().name() + "'")
            + ",'"
            + helpLink.getLink()
            + "');"
            ;
        return s;
    }

    private List<CandidateOpportunityStage> extractCaseStages(Element stageTextElement) {
        List<CandidateOpportunityStage> stages = new ArrayList<>();
        List<String> stageNames = extractStageNames(stageTextElement, false);
        for (String stageName : stageNames) {
            try {
                stages.add(CandidateOpportunityStage.textToEnum(stageName));
            } catch (IllegalArgumentException ex) {
                System.err.println("Could not decode case stage name: " + stageName);
            }
        }
        return stages;
    }

    private List<JobOpportunityStage> extractJobStages(Element stageTextElement) {
        List<JobOpportunityStage> stages = new ArrayList<>();
        List<String> stageNames = extractStageNames(stageTextElement, true);
        for (String stageName : stageNames) {
            try {
                stages.add(JobOpportunityStage.textToEnum(stageName));
            } catch (IllegalArgumentException ex) {
                System.err.println("Could not decode job stage name: " + stageName);
            }
        }
        return stages;
    }

    private List<String> extractStageNames(Element stageTextElement, boolean jobStages) {
        List<String> stageNames = new ArrayList<>();
        Elements lines = stageTextElement.select("p");
        for (Element line : lines) {
            boolean jobOpp = line.text().trim().startsWith("Employer opp");
            if (jobOpp && jobStages || !jobOpp && !jobStages) {
                Elements names = line.select("strong");
                for (Element name : names) {
                    stageNames.add(name.text().trim());
                }
            }
        }
        return stageNames;
    }

    private HelpLink createHelpLink(
        CandidateOpportunityStage caseStage, JobOpportunityStage jobStage,
        String label, String link) {
        HelpLink helpLink = new HelpLink();
        helpLink.setCaseStage(caseStage);
        helpLink.setJobStage(jobStage);
        helpLink.setLabel(label);
        helpLink.setLink(link);
        NextStepInfo nextStepInfo = new NextStepInfo();
        helpLink.setNextStepInfo(nextStepInfo);
        return helpLink;
    }

    private String getDescriptionRowValue(Element table) {
        final Element element = getRowValue("Description", table);
        return element == null ? null : element.text();
    }

    private Element getStageRowValue(Element table) {
        Element stageText = getRowValue("SF Stage", table);
        if (stageText == null) {
            stageText = getRowValue("TC/SF Stage", table);
        }
        return stageText;
    }

    private Element getRowValue(String name, Element table) {
        Elements rows = table.children().select("tr");
        Optional<Element> description = rows.stream()
            //Map rows to columns
            .map(row -> row.children().select("td"))
            .filter(columns -> columns.size() == 2 && (name.equals(columns.get(0).text().trim())))
            .map(columns -> columns.get(1))
            .findFirst();
        return description.orElse(null);
    }

    private Element getHeadingTable(Element heading) {
        Element headingTable = null;
        Element nextElement = heading.nextElementSibling();
        if (nextElement != null && "table".equals(nextElement.tagName())) {
            headingTable = nextElement;
        }
        return headingTable;
    }

    private String getHeadingLink(Element heading) {
        String headingLink = null;
        Elements headingChildren = heading.children().select("a");
        if (headingChildren.size() == 1) {
            headingLink = headingChildren.get(0).attr("href");
        }
        return headingLink;
    }
}
