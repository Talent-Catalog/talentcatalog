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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.User;

class PublishedDocBuilderServiceImplTest {
  PublishedDocBuilderServiceImpl builder;
  Candidate candidate;
  PublishedDocColumnDef infoDependants;
  PublishedDocColumnDef infoId;
  PublishedDocColumnDef infoCN;
  PublishedDocColumnDef infoCV;
  PublishedDocColumnDef infoUser;
  List<PublishedDocColumnDef> columnInfos;

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
    CandidateProperty property = new CandidateProperty();
    property.setName("dependants");
    property.setValue("[{\"user\":\"John\"},{\"user\":\"Jane\"},{\"user\":\"Jill\"}]");
    property.setCandidate(candidate);
    Map<String, CandidateProperty> candidateProperties = new HashMap<>();
    candidateProperties.put(property.getName(), property);
    candidate.setCandidateProperties(candidateProperties);

    columnInfos = new ArrayList<>();

    infoId = addColumn("id", "Candidate id", new PublishedDocFieldSource("id"));

    infoCN = addColumn("cn", "Candidate number",
        new PublishedDocFieldSource("candidateNumber"),
        new PublishedDocFieldSource("shareableCv.location"));

    infoUser = addColumn("name", "Name", new PublishedDocFieldSource("user"));

    infoCV = addColumn("cv", "CV",
        new PublishedDocConstantSource("cv"),
        new PublishedDocFieldSource("shareableCv.location"));

    infoDependants = addColumn("dependants", "Dependants",
        new PublishedDocPropertySource("dependants"));

    ObjectMapper mapper = new ObjectMapper();
    builder = new PublishedDocBuilderServiceImpl(null, mapper);
  }

  @Test
  void buildCellNoExpandingColumn() {
    Object obj;

    obj = builder.buildCell(candidate, null, 0, infoId);
    assertNotNull(obj);
    assertEquals(1234L, obj);

    obj = builder.buildCell(candidate, null, 0, infoUser);
    assertNotNull(obj);
    assertEquals("fred nurk with \n in the middle", obj);

    obj = builder.buildCell(candidate, null, 0, infoCN);
    assertNotNull(obj);
    assertEquals("=HYPERLINK(\"https://candidateCVLink\",1234)", obj);

    obj = builder.buildCell(candidate, null, 0, infoCV);
    assertNotNull(obj);
    assertEquals("=HYPERLINK(\"https://candidateCVLink\",\"cv\")", obj);

    obj = builder.buildCell(candidate, null, 0, infoDependants);
    assertNotNull(obj);
  }

  @Test
  void buildRowNoExpandingColumn() {
    List<Object> row = builder.buildRow(candidate, null, 0, columnInfos);
    assertEquals(columnInfos.size(), row.size());
  }

  @Test
  void buildRowWithExpandingColumn() {
    List<Object> row = builder.buildRow(candidate, infoDependants, 0, columnInfos);
    assertEquals(columnInfos.size(), row.size());

    row = builder.buildRow(candidate, infoDependants, 1, columnInfos);
    assertEquals(columnInfos.size(), row.size());
    assertEquals("John", row.get(2));

    row = builder.buildRow(candidate, infoDependants, 2, columnInfos);
    assertEquals(columnInfos.size(), row.size());
    assertEquals("Jane", row.get(2));

    row = builder.buildRow(candidate, infoDependants, 3, columnInfos);
    assertEquals(columnInfos.size(), row.size());
    assertEquals("Jill", row.get(2));

    //Getting non existing count returns empty string
    row = builder.buildRow(candidate, infoDependants, 4, columnInfos);
    assertEquals(columnInfos.size(), row.size());
    assertEquals("", row.get(2));
  }

  @Test
  void buildTitle() {
    List<Object> title = builder.buildTitle(columnInfos);
    assertEquals(columnInfos.size(), title.size());
  }

  @Test
  void computeNumberOfRowsByCandidate() {
    assertEquals(1, builder.computeNumberOfRowsByCandidate(candidate, null));
    assertEquals(4, builder.computeNumberOfRowsByCandidate(candidate, infoDependants));
  }

  private PublishedDocColumnDef addColumn(String key, String header,
      PublishedDocValueSource value, @Nullable PublishedDocValueSource link) {
    PublishedDocColumnDef info = new PublishedDocColumnDef(key, header);
    info.getContent().setValue(value);
    info.getContent().setLink(link);
    columnInfos.add(info);
    return info;
  }

  private PublishedDocColumnDef addColumn(String key, String header, PublishedDocValueSource value) {
    return addColumn(key, header, value, null);
  }

}
