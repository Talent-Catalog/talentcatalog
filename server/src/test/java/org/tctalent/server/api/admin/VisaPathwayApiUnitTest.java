/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.api.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.util.dto.DtoBuilder;

@ExtendWith(MockitoExtension.class)
class VisaPathwayApiUnitTest {

  @Test
  @DisplayName("list by country id currently returns null")
  void listByCountryIdCurrentlyReturnsNull() {
    VisaPathwayApi api = new VisaPathwayApi();

    assertThat(api.listByCountryId(123L)).isNull();
  }

  @Test
  @DisplayName("visa pathways dto builder is created")
  void visaPathwaysDtoBuilderIsCreated() {
    VisaPathwayApi api = new VisaPathwayApi();

    DtoBuilder dtoBuilder =
        ReflectionTestUtils.invokeMethod(api, "visaPathwaysDto");

    assertThat(dtoBuilder).isNotNull();
  }

  @Test
  @DisplayName("country dto builder is created")
  void countryDtoBuilderIsCreated() {
    VisaPathwayApi api = new VisaPathwayApi();

    DtoBuilder dtoBuilder =
        ReflectionTestUtils.invokeMethod(api, "countryDto");

    assertThat(dtoBuilder).isNotNull();
  }

}