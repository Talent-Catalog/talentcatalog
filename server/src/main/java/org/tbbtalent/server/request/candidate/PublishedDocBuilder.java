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

import org.tbbtalent.server.model.db.Candidate;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to build the published Google sheet doc
 *
 * @author John Cameron
 */
public class PublishedDocBuilder {

  public Object buildCell(Candidate candidate, PublishedDocColumnDef columnInfo) {
    PublishedDocColumnContent columnContent = columnInfo.getContent();
    final PublishedDocValueSource contentValue = columnContent.getValue();
    Object value = contentValue == null ? null : contentValue.fetchData(candidate);
    final PublishedDocValueSource linkSource = columnContent.getLink();

    if (linkSource != null && linkSource.getFieldName() != null) {
      // Check for a link shareable CV, if exists set field name to fetch that cv.
      if (linkSource.getFieldName().equals("shareableCv.url")) {
        if (candidate.getListShareableCv() != null) {
          linkSource.setFieldName("listShareableCv.url");
        }
      }
      // Check for a link shareable Doc, if exists set field name to fetch that doc.
      if (linkSource.getFieldName().equals("shareableDoc.url")) {
        if (candidate.getListShareableDoc() != null) {
          linkSource.setFieldName("listShareableDoc.url");
        }
      }
    }

    String link = linkSource == null ? null : (String) linkSource.fetchData(candidate);

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
}
