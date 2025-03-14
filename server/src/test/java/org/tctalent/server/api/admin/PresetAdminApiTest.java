package org.tctalent.server.api.admin;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tctalent.server.response.preset.PresetGuestTokenResponse;
import org.tctalent.server.service.db.PresetApiService;

/**
 * Unit test for Preset Admin Api endpoints.
 */
@WebMvcTest(PresetAdminApi.class)
@AutoConfigureMockMvc
class PresetAdminApiTest extends ApiTestBase {
  private static final String BASE_PATH = "/api/admin/preset";
  private static final String FETCH_GUEST_TOKEN = "/{dashboardId}/guest-token";

  @Autowired
  MockMvc mockMvc;

  @MockBean
  PresetApiService presetApiService;


  @Test
  void fetchGuestToken_ShouldReturnExpectedObject() throws Exception {
    String dashboardId = "test-dashboard";

    given(presetApiService.fetchGuestToken(dashboardId)).willReturn(new PresetGuestTokenResponse());

    mockMvc.perform(post(BASE_PATH + FETCH_GUEST_TOKEN, dashboardId)
        .with(csrf())
        .header("Authorization", "Bearer " + "jwt-token")
        .accept(MediaType.APPLICATION_JSON))

        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.payload").value(nullValue())); // Verifies return structure
  }

}
