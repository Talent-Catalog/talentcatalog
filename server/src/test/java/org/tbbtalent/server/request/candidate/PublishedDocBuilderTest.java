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
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.model.db.User;

class PublishedDocBuilderTest {
  PublishedDocBuilder builder;
  Candidate candidate;
  PublishedDocColumnInfo infoId;
  PublishedDocColumnInfo infoCN;
  PublishedDocColumnInfo infoCV;
  PublishedDocColumnInfo infoUser;
  List<PublishedDocColumnInfo> columnInfos;

  private PublishedDocColumnInfo addColumn(String key, String header,
      PublishedDocValueSource value, @Nullable PublishedDocValueSource link) {
    PublishedDocColumnInfo info = new PublishedDocColumnInfo(key, header);
    info.getContent().setValue(value);
    info.getContent().setLink(link);
    columnInfos.add(info);
    return info;
  }

  private PublishedDocColumnInfo addColumn(String key, String header, PublishedDocValueSource value) {
    return addColumn(key, header, value, null);
  }

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

    infoId = addColumn("id", "Candidate id", new PublishedDocFieldSource("id"));
    
    infoCN = addColumn("cn", "Candidate number", 
        new PublishedDocFieldSource("candidateNumber"), 
        new PublishedDocFieldSource("shareableCv.location"));
    
    infoUser = addColumn("name", "Name", new PublishedDocFieldSource("user"));
    
    infoCV = addColumn("cv", "CV",
        new PublishedDocConstantSource("cv"),
        new PublishedDocFieldSource("shareableCv.location"));
    
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