/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import org.tctalent.server.model.db.HelpLink;
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
     *         Stage(s) row - job or case opp stages - or both. This is unstructured and will need
     *         to be manually converted to correct stages
     *     </li>
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
                            String stageText = getStageRowValue(headingTable);
                            if (stageText != null) {

                                //Each heading corresponds to a HelpLink
                                HelpLink helpLink = new HelpLink();
                                helpLinks.add(helpLink);

                                //Use heading text as the label of the HelpLink
                                helpLink.setLabel(headingText);

                                //Use the heading link as the link of the HelpLink
                                helpLink.setLink(url + headingLink);

                                NextStepInfo nextStepInfo = new NextStepInfo();
                                helpLink.setNextStepInfo(nextStepInfo);
                                nextStepInfo.setNextStepText(description);

                                //TODO JC Decode stage text
                                System.out.println(stageText);

                                //TODO JC Need to tidy up ops text. Changes label from SF Stage to TC/SF Stage

                            }
                        }
                    }
                }
            }
        }

        System.out.println(helpLinks.size());
    }

    private String getDescriptionRowValue(Element table) {
        return getRowValue("Description", table);
    }

    private String getStageRowValue(Element table) {
        return getRowValue("SF Stage", table);
    }

    private String getRowValue(String name, Element table) {
        Elements rows = table.children().select("tr");
        Optional<String> description = rows.stream()
            //Map rows to columns
            .map(row -> row.children().select("td"))
            .filter(columns -> columns.size() == 2 && (name.equals(columns.get(0).text().trim())))
            .map(columns -> columns.get(1).text())
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
