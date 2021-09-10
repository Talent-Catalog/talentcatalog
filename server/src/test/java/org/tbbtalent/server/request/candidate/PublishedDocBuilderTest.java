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
import org.tbbtalent.server.model.db.User;

class PublishedDocBuilderTest {
  PublishedDocBuilder builder;
  Candidate candidate;
  PublishedDocColumnInfo infoId;
  PublishedDocColumnInfo infoCN;
  List<PublishedDocColumnInfo> columnInfos;
  
  @BeforeEach
  void setUp() {

    candidate = new Candidate();
    candidate.setId(1234L);
    candidate.setCandidateNumber("1234");
    User user = new User();
    candidate.setUser(user);
    user.setFirstName("fred");
    user.setLastName("nurk with \n in the middle");
    
    columnInfos = new ArrayList<>();
    PublishedDocColumnInfo info;
    PublishedDocColumnContent content;

    infoId = new PublishedDocColumnInfo();
    infoId.setHeader("Candidate id");
    content = new PublishedDocColumnContent();
    content.setValueSource(new PublishedDocFieldSource("id"));
    infoId.setColumnContent(content);
    columnInfos.add(infoId);
    
    infoCN = new PublishedDocColumnInfo();
    infoCN.setHeader("Candidate number");
    content = new PublishedDocColumnContent();
    content.setValueSource(new PublishedDocFieldSource("candidateNumber"));
    content.setLink("https://www.talentbeyondboundaries.org/");
    infoCN.setColumnContent(content);
    columnInfos.add(infoCN);
    
    info = new PublishedDocColumnInfo();
    info.setHeader("Name");
    content = new PublishedDocColumnContent();
    content.setValueSource(new PublishedDocFieldSource("user"));
    info.setColumnContent(content);
    columnInfos.add(info);
    
    info = new PublishedDocColumnInfo();
    info.setHeader("CV");
    content = new PublishedDocColumnContent();
    content.setValueSource(new PublishedDocConstantSource("cv"));
    content.setLink("https://www.talentbeyondboundaries.org/");
    info.setColumnContent(content);
    columnInfos.add(info);
    
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
    assertEquals("=HYPERLINK(\"https://www.talentbeyondboundaries.org/\",1234)", obj);
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