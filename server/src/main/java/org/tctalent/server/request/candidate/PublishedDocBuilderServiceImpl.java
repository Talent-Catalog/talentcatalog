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

package org.tctalent.server.request.candidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.HasMultipleRows;
import org.tctalent.server.security.CandidateTokenProvider;

/**
 * Used to build the published Google sheet doc
 *
 * @author John Cameron
 */
@Service
@Slf4j
public class PublishedDocBuilderServiceImpl implements PublishedDocBuilderService {

    private final CandidateTokenProvider candidateTokenProvider;

    public PublishedDocBuilderServiceImpl(
        CandidateTokenProvider candidateTokenProvider) {
        this.candidateTokenProvider = candidateTokenProvider;
    }

    public Object buildCell(Candidate candidate, @Nullable PublishedDocColumnDef expandingColumnDef,
        int expandingCount, PublishedDocColumnDef columnInfo) {

        PublishedDocColumnContent columnContent = columnInfo.getContent();
        final PublishedDocValueSource valueSource = columnContent.getValue();
        final PublishedDocValueSource linkSource = columnContent.getLink();

        Object value = null;
        String link = null;
        if (expandingCount == 0) {
            value = valueSource == null ? null : fetchData(candidate, valueSource);
            link = linkSource == null ? null : (String) fetchData(candidate, linkSource);
        } else {
            HasMultipleRows expandData = getExpandingData(candidate, expandingColumnDef);
            if (expandData != null) {
                value = valueSource == null ? null : expandData.get(expandingCount, valueSource);
                link =
                    linkSource == null ? null : (String) expandData.get(expandingCount, linkSource);
            }
        }

        if (link == null || value == null) {
            return value == null ? "" : value;
        } else {
            //String values need to be quoted - otherwise no quotes so that numbers still display as numbers.
            String quotedValue = value instanceof String ? "\"" + value + "\"" : value.toString();
            return "=HYPERLINK(\"" + link + "\"," + quotedValue + ")";
        }
    }

    public List<Object> buildRow(
        Candidate candidate, @Nullable PublishedDocColumnDef expandingColumnDef,
        int expandingCount, List<PublishedDocColumnDef> columnInfos) {
        List<Object> candidateData = new ArrayList<>();
        for (PublishedDocColumnDef columnInfo : columnInfos) {
            Object obj = buildCell(candidate, expandingColumnDef, expandingCount, columnInfo);
            candidateData.add(obj);
        }
        return candidateData;
    }

    public List<Object> buildTitle(List<PublishedDocColumnDef> columnInfos) {
        List<Object> title = new ArrayList<>();
        for (PublishedDocColumnDef columnInfo : columnInfos) {
            title.add(columnInfo.getHeader());
        }
        return title;
    }

    @Override
    public int computeNumberOfRowsByCandidate(@NonNull Candidate candidate,
        @Nullable PublishedDocColumnDef expandingColumnDef) {

        //Always one row for the candidate
        int nRows = 1;

        //Look at value to check if it has multiple values
        HasMultipleRows obj = getExpandingData(candidate, expandingColumnDef);
        if (obj != null) {
            nRows += obj.nRows();
        }
        return nRows;
    }

    /**
     * Retrieves the data that is the value of this value source corresponding to the given
     * candidate.
     *
     * @param candidate   Candidate - only used for field value sources
     * @param valueSource Source of the data
     * @return the value
     */
    @Nullable
    private Object fetchData(Candidate candidate, @NonNull PublishedDocValueSource valueSource) {
        Object val = null;
        final String fieldName = valueSource.getFieldName();
        final String propertyName = valueSource.getPropertyName();
        if (fieldName != null) {
            if (candidate == null) {
                LogBuilder.builder(log)
                    .action("PublishedDocBuilderService")
                    .message("Cannot extract field " + fieldName + " from null candidate")
                    .logError();
            } else {
                try {
                    // Get the list specific shareable CV or Doc if exists, otherwise get the field name supplied.
                    if (fieldName.equals("shareableCv.url")
                        && candidate.getListShareableCv() != null) {
                        val = candidate.extractField("listShareableCv.url");
                    } else if (fieldName.equals("shareableDoc.url")
                        && candidate.getListShareableDoc() != null) {
                        val = candidate.extractField("listShareableDoc.url");
                    } else if (fieldName.equals("autoCvLink")) {
                        val = "https://tctalent.org/public-portal/cv/"
                            + candidateTokenProvider.generateToken(
                            candidate.getCandidateNumber(), 365L);
                    } else if (fieldName.equals("smartCvLink")) {
                        // If a candidate has a shareable CV use that link, otherwise use the autogen CV link.
                        if (candidate.getListShareableCv() != null) {
                            val = candidate.extractField("listShareableCv.url");
                        } else if (candidate.getShareableCv() != null) {
                            val = candidate.extractField("shareableCv.url");
                        } else {
                            val = "https://tctalent.org/public-portal/cv/"
                                + candidateTokenProvider.generateToken(
                                candidate.getCandidateNumber(), 365L);
                        }
                    } else {
                        val = candidate.extractField(fieldName);
                    }
                } catch (Exception e) {
                    LogBuilder.builder(log)
                        .action("PublishedDocBuilderService")
                        .message("Error extracting field " + fieldName + " from candidate "
                            + candidate.getCandidateNumber())
                        .logError();
                }
            }
        } else if (propertyName != null) {
            //Check for the specific candidate property with the property name provided
            Map<String, CandidateProperty> properties = candidate.getCandidateProperties();
            if (properties != null) {
                //Fetch the value
                CandidateProperty property = properties.get(propertyName);
                if (property != null) {
                    val = property.getValue();
                }
            }
        } else {
            val = valueSource.getConstant();
        }
        return val;
    }

    /**
     * Retrieve the multi valued data value corresponding to the given PublishedDocColumnDef
     *
     * @param candidate Candidate
     * @param columnDef Column definition
     * @return the multi valued data or null if no such data is found.
     */
    @Nullable
    private HasMultipleRows getExpandingData(
        @NonNull Candidate candidate, @Nullable PublishedDocColumnDef columnDef) {
        HasMultipleRows obj = null;
        if (columnDef != null) {
            final PublishedDocValueSource value = columnDef.getContent().getValue();
            if (value != null) {
                final Object o = fetchData(candidate, value);
                if (o instanceof HasMultipleRows) {
                    obj = (HasMultipleRows) o;
                }
            }
        }
        return obj;
    }
}
