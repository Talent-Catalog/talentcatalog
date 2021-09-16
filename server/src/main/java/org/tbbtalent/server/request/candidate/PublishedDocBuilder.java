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

import com.google.api.client.util.Data;
import org.tbbtalent.server.model.db.Candidate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used to build the published Google sheet doc
 *
 * @author John Cameron
 */
public class PublishedDocBuilder {

  public Object buildCell(Candidate candidate, PublishedDocColumnInfo columnInfo) {
    PublishedDocColumnContent columnContent = columnInfo.getContent();
    //Object value = columnContent.getValue().fetchData(candidate);

    final PublishedDocValueSource valueSource = columnContent.getValue();
    Object value = valueSource == null ? null : valueSource.fetchData(candidate);

    final PublishedDocValueSource linkSource = columnContent.getLink();
    String link = linkSource == null ? null : (String) linkSource.fetchData(candidate);

    if (link == null || value == null) {
      // Handle null values in the table to avoid the cell ignored and overwritten
      if (value == null) {
        return Data.NULL_STRING;
      } else {
        return value;
      }
    } else {
      //String values need to be quoted - otherwise no quotes so that numbers still display as numbers.
      String quotedValue = value instanceof String ? "\"" + value + "\"" : value.toString();
      return "=HYPERLINK(\"" + link + "\"," + quotedValue + ")";
    }
  }

  public List<Object> buildRow(Candidate candidate, List<PublishedDocColumnInfo> columnInfos) {
    List<Object> candidateData = new ArrayList<>();
    for (PublishedDocColumnInfo columnInfo : columnInfos) {
      Object obj = buildCell(candidate, columnInfo);
      candidateData.add(obj);
    }
    return candidateData;
  }

  public List<Object> buildTitle(List<PublishedDocColumnInfo> columnInfos) {
    List<Object> title = new ArrayList<>();
    for (PublishedDocColumnInfo columnInfo : columnInfos) {
      title.add(columnInfo.getHeader());
    }
    return title;
  }

  public boolean doesCandidateObjectContainField(Candidate candidate, String fieldName) {
    return Arrays.stream(candidate.getClass().getFields())
            .anyMatch(f -> f.getName().equals(fieldName));
  }
}
