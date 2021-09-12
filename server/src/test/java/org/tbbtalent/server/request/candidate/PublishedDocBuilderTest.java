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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.model.db.User;

class PublishedDocBuilderTest {
  PublishedDocBuilder builder;
  Candidate candidate;
  PublishedDocColumnInfo infoId;
  PublishedDocColumnInfo infoCN;
  PublishedDocColumnInfo infoCV;
  List<PublishedDocColumnInfo> columnInfos;
  
  @BeforeEach
  void setUp() {

    candidate = new Candidate();
    candidate.setId(1234L);
    candidate.setCandidateNumber("1234");
    CandidateAttachment attachment = new CandidateAttachment();
    attachment.setLocation("https://candidateCVLink");
    candidate.setShareableCv(attachment);
    User user = new User();
    candidate.setUser(user);
    user.setFirstName("fred");
    user.setLastName("nurk with \n in the middle");
    
    columnInfos = new ArrayList<>();
    PublishedDocColumnInfo info;
    PublishedDocColumnContent content;
    PublishedDocValueSource linkSource;
    PublishedDocValueSource valueSource;

    infoId = new PublishedDocColumnInfo("Candidate id");
    valueSource = new PublishedDocValueSource();
    valueSource.setFieldName("id");
    content = new PublishedDocColumnContent(valueSource);
    infoId.setColumnContent(content);
    columnInfos.add(infoId);
    
    infoCN = new PublishedDocColumnInfo("Candidate number");
    valueSource = new PublishedDocValueSource();
    valueSource.setFieldName("candidateNumber");
    linkSource = new PublishedDocValueSource();
    linkSource.setFieldName("shareableCv.location");
    content = new PublishedDocColumnContent(valueSource, linkSource);
    infoCN.setColumnContent(content);
    columnInfos.add(infoCN);
    
    info = new PublishedDocColumnInfo("Name");
    valueSource = new PublishedDocValueSource();
    valueSource.setFieldName("user");
    content = new PublishedDocColumnContent(valueSource);
    info.setColumnContent(content);
    columnInfos.add(info);
    
    infoCV = new PublishedDocColumnInfo("CV");
    valueSource = new PublishedDocValueSource();
    valueSource.setConstant("cv");
    linkSource = new PublishedDocValueSource();
    linkSource.setFieldName("shareableCv.location");
    content = new PublishedDocColumnContent(valueSource, linkSource);
    infoCV.setColumnContent(content);
    columnInfos.add(infoCV);
    
    builder = new PublishedDocBuilder();
  }

  @Test
  void buildCell() {
    Object obj;
    
    obj = builder.buildCell(candidate, infoId);
    assertNotNull(obj);
    assertEquals(1234L, obj);
    
    obj = builder.buildCell(candidate, infoCN);
    assertNotNull(obj);
    assertEquals("=HYPERLINK(\"https://candidateCVLink\",1234)", obj);
    
    obj = builder.buildCell(candidate, infoCV);
    assertNotNull(obj);
    assertEquals("=HYPERLINK(\"https://candidateCVLink\",\"cv\")", obj);
  }

  @Test
  void buildRow() {
    List<Object> row = builder.buildRow(candidate, columnInfos);
    assertEquals(columnInfos.size(), row.size());
  }

  @Test
  void buildTitle() {
    List<Object> title = builder.buildTitle(columnInfos);
    assertEquals(columnInfos.size(), title.size());
  }
}