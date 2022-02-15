/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request.candidate;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.Candidate;

/**
 * Used to build the published Google sheet doc
 *
 * @author John Cameron
 */
@Service
public class PublishedDocBuilderServiceImpl implements PublishedDocBuilderService {
  private static final Logger log = LoggerFactory.getLogger(PublishedDocBuilderServiceImpl.class);

  public PublishedDocBuilderServiceImpl() {
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


  /**
   * Retrieves the data that is the value of this value source corresponding to the given candidate.
   * @param candidate Candidate - only used for field value sources
   * @return the value
   */
  @Nullable
  private Object fetchData(Candidate candidate, PublishedDocValueSource valueSource) {
    Object val = null;
    final String fieldName = valueSource.getFieldName();
    if (fieldName != null) {
      if (candidate == null) {
        log.error("Cannot extract field " + fieldName + " from null candidate");
      } else {
        try {
          // Get the list specific shareable CV or Doc if exists, otherwise get the field name supplied.
          if (fieldName.equals("shareableCv.url") && candidate.getListShareableCv() != null) {
            val = candidate.extractField("listShareableCv.url");
          } else if (fieldName.equals("shareableDoc.url")
              && candidate.getListShareableDoc() != null) {
            val = candidate.extractField("listShareableDoc.url");
          } else {
            val = candidate.extractField(fieldName);
          }
        } catch (Exception e) {
          log.error("Error extracting field " + fieldName + " from candidate " + candidate.getCandidateNumber());
        }
      }
    } else {
      val = valueSource.getConstant();
    }
    return val;
  }

}
