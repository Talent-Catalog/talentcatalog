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

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
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
    candidate.setId(1234L);
    candidate.setCandidateNumber("1234");
    User user = new User();
    candidate.setUser(user);
    user.setFirstName("fred");
    user.setLastName("nurk with \n in the middle");

    writer = new StringWriter();
  }

  @Test
  void extractFieldCallWithNull() {
    try {
      candidate.extractField(null);
      fail("Null exportField should throw exception ");
    } catch (Exception e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }

  @Test
  void extractObjectTypeTest()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Object obj;

    obj = candidate.extractField("id");
    assertNotNull(obj);
    //Id is a number
    assertEquals(Long.class, obj.getClass());

    obj = candidate.extractField("candidateNumber");
    assertNotNull(obj);
    //CandidateNumber should have been extracted as a number
    assertEquals(Long.class, obj.getClass());

    obj = candidate.extractField("user");
    assertNotNull(obj);
    //User is extracted as a String
    assertEquals(String.class, obj.getClass());

    obj = candidate.extractField("user.firstName");
    assertNotNull(obj);
    //First name is a String
    assertEquals(String.class, obj.getClass());

  }
}
