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

package org.tbbtalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the candidate extractFields method and csv creation
 *
 * @author John Cameron
 */
class CandidateTestExtractFields {
  private Candidate candidate;
  private Writer writer;
  
  @BeforeEach
  void setUp() {
    candidate = new Candidate();
    User user = new User();
    candidate.setUser(user);
    user.setFirstName("fred");
    user.setLastName("nurk with \n in the middle");
    
    writer = new StringWriter();
  }

  @Test
  void extractFields() {

    List<String> exportFields = Arrays.asList("user", "user.firstName", "user.lastName");

    try {
      List<String> extracts = candidate.extractFields(exportFields);
      assertNotNull(extracts);
      assertEquals(exportFields.size(), extracts.size());
    } catch (Exception e) {
      fail("Failed extracting " + exportFields + " from " + candidate, e);
    }
  }

  @Test
  void extractFieldsCallWithNull() {

    try {
      List<String> extracts = candidate.extractFields(null);
      assertNotNull(extracts);
      assertEquals(0, extracts.size());
    } catch (Exception e) {
      fail("Failed extracting null exportFields from " + candidate, e);
    }
  }

  @Test
  void exportToCsv()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IOException {
    List<String> exportFields = Arrays.asList("user", "user.firstName", "user.lastName");
    List<String> extracts = candidate.extractFields(exportFields);


    CSVWriter csvWriter = new CSVWriter(writer);
    
    //Title line
    csvWriter.writeNext(exportFields.toArray(new String[0]));

    csvWriter.writeNext(extracts.toArray(new String[0]));
    
    String s = writer.toString();
    
    assertNotNull(s);
    
    csvWriter.close();
  }
}