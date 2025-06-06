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

package org.tctalent.server.api.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.tctalent.server.data.CountryTestData.getSourceCountryList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.request.country.SearchCountryRequest;
import org.tctalent.server.request.country.UpdateCountryRequest;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Unit tests for Country Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CountryAdminApi.class)
@AutoConfigureMockMvc
class CountryAdminApiTest extends ApiTestBase {

  private static final long COUNTRY_ID = 465L;

  private static final String BASE_PATH = "/api/admin/country";
  private static final String RESTRICTED_LIST_PATH = "/restricted";
  private static final String DESTINATIONS_LIST_PATH = "/destinations";
  private static final String SEARCH_PAGED_PATH = "/search-paged";

  private static final List<Country> countries = getSourceCountryList();

  private final Page<Country> countryPage =
      new PageImpl<>(
          countries,
          PageRequest.of(0,10, Sort.unsorted()),
          1
      );

  @MockBean CountryService countryService;

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired CountryAdminApi countryAdminApi;

  @BeforeEach
  void setUp() {
    configureAuthentication();
    given(countryService
        .selectBuilder())
        .willReturn(new DtoBuilder().add("name").add("status"));
  }

  @Test
  public void testWebOnlyContextLoads() {
    assertThat(countryAdminApi).isNotNull();
  }

  @Test
  @DisplayName("get countries succeeds")
  void getCountriesSucceeds() throws Exception {
    given(countryService
        .listCountries(false))
        .willReturn(countries);

    mockMvc.perform(get(BASE_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Lebanon")))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[1].name", is("Jordan")))
        .andExpect(jsonPath("$[1].status", is("active")));

    verify(countryService).listCountries(false);
  }

  @Test
  @DisplayName("get restricted countries succeeds")
  void getRestrictedCountriesSucceeds() throws Exception {
    given(countryService
        .listCountries(true))
        .willReturn(countries);

    mockMvc.perform(get(BASE_PATH + "/" + RESTRICTED_LIST_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Lebanon")))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[1].name", is("Jordan")))
        .andExpect(jsonPath("$[1].status", is("active")));

    verify(countryService).listCountries(true);
  }

  @Test
  @DisplayName("get destination countries succeeds")
  void getDestinationCountriesSucceeds() throws Exception {
    given(countryService
        .getTCDestinations())
        .willReturn(countries);

    mockMvc.perform(get(BASE_PATH + "/" + DESTINATIONS_LIST_PATH)
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Lebanon")))
        .andExpect(jsonPath("$[0].status", is("active")))
        .andExpect(jsonPath("$[1].name", is("Jordan")))
        .andExpect(jsonPath("$[1].status", is("active")));


    verify(countryService).getTCDestinations();
  }

  @Test
  @DisplayName("search paged countries succeeds")
  void searchPagedCountriesSucceeds() throws Exception {
    SearchCountryRequest request = new SearchCountryRequest();

    given(countryService
        .searchCountries(any(SearchCountryRequest.class)))
        .willReturn(countryPage);

    mockMvc.perform(post(BASE_PATH + SEARCH_PAGED_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.totalElements", is(2)))
        .andExpect(jsonPath("$.totalPages", is(1)))
        .andExpect(jsonPath("$.number", is(0)))
        .andExpect(jsonPath("$.hasNext", is(false)))
        .andExpect(jsonPath("$.hasPrevious", is(false)))
        .andExpect(jsonPath("$.content", notNullValue()))
        .andExpect(jsonPath("$.content.[0].name", is("Lebanon")))
        .andExpect(jsonPath("$.content.[0].status", is("active")))
        .andExpect(jsonPath("$.content.[1].name", is("Jordan")))
        .andExpect(jsonPath("$.content.[1].status", is("active")));

    verify(countryService).searchCountries(any(SearchCountryRequest.class));
  }

  @Test
  @DisplayName("get country by id succeeds")
  void getCountryByIdSucceeds() throws Exception {

    given(countryService
        .getCountry(COUNTRY_ID))
        .willReturn(new Country("Ukraine", Status.active));

    mockMvc.perform(get(BASE_PATH + "/" + COUNTRY_ID)
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Ukraine")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(countryService).getCountry(COUNTRY_ID);
  }

  @Test
  @DisplayName("create country succeeds")
  void createCountrySucceeds() throws Exception {
    UpdateCountryRequest request = new UpdateCountryRequest();
    request.setName("Ukraine");
    request.setStatus(Status.active);

    given(countryService
        .createCountry(any(UpdateCountryRequest.class)))
        .willReturn(new Country("Ukraine", Status.active));

    mockMvc.perform(post(BASE_PATH)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Ukraine")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(countryService).createCountry(any(UpdateCountryRequest.class));
  }

  @Test
  @DisplayName("update country succeeds")
  void updateCountrySucceeds() throws Exception {
    UpdateCountryRequest request = new UpdateCountryRequest();
    request.setName("Ukraine");
    request.setStatus(Status.active);

    given(countryService
        .updateCountry(anyLong(), any(UpdateCountryRequest.class)))
        .willReturn(new Country("Ukraine", Status.active));

    mockMvc.perform(put(BASE_PATH + "/" + COUNTRY_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$.name", is("Ukraine")))
        .andExpect(jsonPath("$.status", is("active")));

    verify(countryService).updateCountry(anyLong(), any(UpdateCountryRequest.class));
  }

  @Test
  @DisplayName("delete country by id succeeds")
  void deleteCountryByIdSucceeds() throws Exception {

    given(countryService
        .deleteCountry(COUNTRY_ID))
        .willReturn(true);

    mockMvc.perform(delete(BASE_PATH + "/" + COUNTRY_ID)
            .with(csrf())
            .header("Authorization", "Bearer " + "jwt-token")
            .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", notNullValue()))
        .andExpect(jsonPath("$", is(true)));

    verify(countryService).deleteCountry(COUNTRY_ID);
  }

}
