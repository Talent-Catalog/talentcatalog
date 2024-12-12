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

package org.tctalent.server.model.sf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContactTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("convert json string to contact pojo succeeds")
    void jsonStringToContactPojo() throws JsonProcessingException {
        String contactJson = """
                {
                  "AccountId": "12345",
                  "TBBid__c": "67890"
                }""";

        Contact contact = objectMapper.readValue(contactJson, Contact.class);

        assertThat(contact.getAccountId()).isEqualTo("12345");
        assertThat(contact.getTbbId()).isEqualTo(67890L);
    }

}
