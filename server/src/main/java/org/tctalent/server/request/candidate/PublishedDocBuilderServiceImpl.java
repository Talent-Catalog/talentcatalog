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

  public Object buildCell(Candidate candidate, PublishedDocColumnDef columnInfo) {
    PublishedDocColumnContent columnContent = columnInfo.getContent();
    final PublishedDocValueSource contentValue = columnContent.getValue();
    Object value = contentValue == null ? null : fetchData(candidate, contentValue);
    final PublishedDocValueSource linkSource = columnContent.getLink();

    String link = linkSource == null ? null : (String) fetchData(candidate, linkSource);

    if (link == null || value == null) {
      return value == null ? "" : value;
    } else {
      //String values need to be quoted - otherwise no quotes so that numbers still display as numbers.
      String quotedValue = value instanceof String ? "\"" + value + "\"" : value.toString();
      return "=HYPERLINK(\"" + link + "\"," + quotedValue + ")";
    }
  }

  public List<Object> buildRow(Candidate candidate, List<PublishedDocColumnDef> columnInfos) {
    List<Object> candidateData = new ArrayList<>();
    for (PublishedDocColumnDef columnInfo : columnInfos) {
      //TODO JC Second loop through dependants
      Object obj = buildCell(candidate, columnInfo);
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
      @NonNull PublishedDocColumnDef expandingColumnDef) {

    //Always one row for the candidate
    int nRows = 1;

    final PublishedDocValueSource value = expandingColumnDef.getContent().getValue();
    Object obj = value == null ? null : fetchData(candidate, value);
    if (obj != null) {
      //TODO JC Figure out what it is and if it contributes extra rows
    }
    return nRows;
  }

  /**
   * Retrieves the data that is the value of this value source corresponding to the given candidate.
   * @param candidate Candidate - only used for field value sources
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
          if (fieldName.equals("shareableCv.url") && candidate.getListShareableCv() != null) {
            val = candidate.extractField("listShareableCv.url");
          } else if (fieldName.equals("shareableDoc.url") && candidate.getListShareableDoc() != null) {
            val = candidate.extractField("listShareableDoc.url");
          } else if (fieldName.equals("autoCvLink")) {
            val = "https://tctalent.org/public-portal/cv/" + candidateTokenProvider.generateToken(
                    candidate.getCandidateNumber(), 365L);
          } else if(fieldName.equals("smartCvLink")) {
            // If a candidate has a shareable CV use that link, otherwise use the autogen CV link.
            if (candidate.getListShareableCv() != null) {
              val = candidate.extractField("listShareableCv.url");
            } else if (candidate.getShareableCv() != null) {
              val = candidate.extractField("shareableCv.url");
            } else {
              val = "https://tctalent.org/public-portal/cv/" + candidateTokenProvider.generateToken(
                      candidate.getCandidateNumber(), 365L);
            }
          } else {
            val = candidate.extractField(fieldName);
          }
        } catch (Exception e) {
          LogBuilder.builder(log)
              .action("PublishedDocBuilderService")
              .message("Error extracting field " + fieldName + " from candidate " + candidate.getCandidateNumber())
              .logError();
        }
      }
    } else if (propertyName != null) {
      //Check for the specific candidate property with the property name provided
      Map<String,CandidateProperty> properties = candidate.getCandidateProperties();
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

}
