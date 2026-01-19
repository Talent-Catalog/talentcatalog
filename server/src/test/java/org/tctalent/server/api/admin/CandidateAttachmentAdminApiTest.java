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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.request.attachment.CreateCandidateAttachmentRequest;
import org.tctalent.server.request.attachment.ListByUploadTypeRequest;
import org.tctalent.server.request.attachment.SearchByIdCandidateAttachmentRequest;
import org.tctalent.server.request.attachment.SearchCandidateAttachmentsRequest;
import org.tctalent.server.request.attachment.UpdateCandidateAttachmentRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateAttachmentService;

/**
 * Unit tests for Candidate Attachment Admin Api endpoints.
 *
 * @author sadatmalik
 */
@WebMvcTest(CandidateAttachmentAdminApi.class)
@AutoConfigureMockMvc
class CandidateAttachmentAdminApiTest extends ApiTestBase {
    private static final long CANDIDATE_ID = 99L;
    private static final String BASE_PATH = "/api/admin/candidate-attachment";
    private static final String SEARCH_PATH = "/search";
    private static final String SEARCH_PAGED_PATH = "/search-paged";
    private static final String DOWNLOAD_BY_ID_PATH = "/{id}/download";
    private static final String UPLOAD_BY_ID_PATH = "/{id}/upload";
    private static final String LIST_BY_TYPE_PATH = "/list-by-type";

    private static final CandidateAttachment candidateAttachmentsCvOnly = getCandidateAttachments(true);
    private static final CandidateAttachment candidateAttachments = getCandidateAttachments(false);

    private final Page<CandidateAttachment> pageCandidateAttachments =
            new PageImpl<>(
                    List.of(candidateAttachments, candidateAttachmentsCvOnly),
                    PageRequest.of(0,10, Sort.unsorted()),
                    2
            );

    @MockBean CandidateAttachmentService candidateAttachmentService;
    @MockBean
    AuthService authService;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CandidateAttachmentAdminApi candidateAttachmentAdminApi;

    @BeforeEach
    void setUp() {
        configureAuthentication();
    }

    @Test
    public void testWebOnlyContextLoads() {
        assertThat(candidateAttachmentAdminApi).isNotNull();
    }

    @Test
    @DisplayName("search succeeds")
    void searchSucceeds() throws Exception {
        SearchByIdCandidateAttachmentRequest request = new SearchByIdCandidateAttachmentRequest();
        request.setCandidateId(1L);
        request.setCvOnly(false);

        given(candidateAttachmentService
                .listCandidateAttachments(anyLong()))
                .willReturn(List.of(candidateAttachments));

        mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].cv", is(false)));

        verify(candidateAttachmentService).listCandidateAttachments(anyLong());
    }

    @Test
    @DisplayName("search cv only succeeds")
    void searchCvOnlySucceeds() throws Exception {
        SearchByIdCandidateAttachmentRequest request = new SearchByIdCandidateAttachmentRequest();
        request.setCandidateId(CANDIDATE_ID);
        request.setCvOnly(true);

        given(candidateAttachmentService
                .listCandidateCvs(anyLong()))
                .willReturn(List.of(candidateAttachmentsCvOnly));

        mockMvc.perform(post(BASE_PATH + SEARCH_PATH)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].cv", is(true)));

        verify(candidateAttachmentService).listCandidateCvs(anyLong());
    }

    @Test
    @DisplayName("search paged succeeds")
    void searchPagedSucceeds() throws Exception {
        SearchCandidateAttachmentsRequest request = new SearchCandidateAttachmentsRequest();
        request.setCandidateId(CANDIDATE_ID);

        given(candidateAttachmentService
                .searchCandidateAttachments(any(SearchCandidateAttachmentsRequest.class)))
                .willReturn(pageCandidateAttachments);

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
                .andExpect(jsonPath("$.content.[0].cv", is(false)))
                .andExpect(jsonPath("$.content.[1].cv", is(true)));

        verify(candidateAttachmentService).searchCandidateAttachments(any(SearchCandidateAttachmentsRequest.class));
    }

    @Test
    @DisplayName("create candidate attachment succeeds")
    void createAttachmentSucceeds() throws Exception {
        CreateCandidateAttachmentRequest request = new CreateCandidateAttachmentRequest();

        given(candidateAttachmentService
                .createCandidateAttachment(any(CreateCandidateAttachmentRequest.class)))
                .willReturn(candidateAttachments);

        mockMvc.perform(post(BASE_PATH)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.cv", is(false)));

        verify(candidateAttachmentService).createCandidateAttachment(any(CreateCandidateAttachmentRequest.class));
    }

    @Test
    @DisplayName("download by id succeeds")
    void downloadByIdSucceeds() throws Exception {
        CandidateAttachment googleDoc = getCandidateAttachments(true);
        googleDoc.setType(AttachmentType.googlefile);

        given(candidateAttachmentService
                .getCandidateAttachment(anyLong()))
                .willReturn(googleDoc);

        mockMvc.perform(get(BASE_PATH + DOWNLOAD_BY_ID_PATH.replace("{id}", Long.toString(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(candidateAttachmentService).getCandidateAttachment(anyLong());
        verify(candidateAttachmentService).downloadCandidateAttachment(any(CandidateAttachment.class), any(OutputStream.class));
    }

    @Test
    @DisplayName("download by id fails - not a google file type")
    void downloadByIdFailsForNonGoogleFileType() throws Exception {
        given(candidateAttachmentService
                .getCandidateAttachment(anyLong()))
                .willReturn(candidateAttachments);

        mockMvc.perform(get(BASE_PATH + DOWNLOAD_BY_ID_PATH.replace("{id}", Long.toString(CANDIDATE_ID)))
                        .header("Authorization", "Bearer " + "jwt-token")
                        .accept(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("missing_object")))
                .andExpect(jsonPath("$.message", is("Missing FileSystemService with ID 99")));

        verify(candidateAttachmentService).getCandidateAttachment(anyLong());
        verify(candidateAttachmentService, never()).downloadCandidateAttachment(any(CandidateAttachment.class), any(OutputStream.class));
    }

    @Test
    @DisplayName("upload candidate attachment succeeds")
    void uploadAttachmentSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile("cv", "cv.txt", "text/plain", "some cv text".getBytes());

        given(candidateAttachmentService
                .uploadAttachment(anyLong(), anyBoolean(), any(MultipartFile.class)))
                .willReturn(candidateAttachmentsCvOnly);

        mockMvc.perform(multipart(BASE_PATH + UPLOAD_BY_ID_PATH.replace("{id}", Long.toString(CANDIDATE_ID)))
                        .file("file", file.getBytes())
                        .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .param("cv", "true"))


                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.cv", is(true)));

        verify(candidateAttachmentService).uploadAttachment(anyLong(), anyBoolean(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("update candidate attachment succeeds")
    void updateAttachmentSucceeds() throws Exception {
        UpdateCandidateAttachmentRequest request = new UpdateCandidateAttachmentRequest();

        given(candidateAttachmentService
                .updateCandidateAttachment(anyLong(), any(UpdateCandidateAttachmentRequest.class)))
                .willReturn(candidateAttachments);

        mockMvc.perform(put(BASE_PATH + "/" + CANDIDATE_ID)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.cv", is(false)));

        verify(candidateAttachmentService).updateCandidateAttachment(anyLong(), any(UpdateCandidateAttachmentRequest.class));
    }

    @Test
    @DisplayName("list by upload type succeeds")
    void listByUploadTypeSucceeds() throws Exception {
        ListByUploadTypeRequest request = new ListByUploadTypeRequest();

        given(candidateAttachmentService
                .listCandidateAttachmentsByType(any(ListByUploadTypeRequest.class)))
                .willReturn(List.of(candidateAttachments));

        mockMvc.perform(post(BASE_PATH + LIST_BY_TYPE_PATH)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].cv", is(false)));

        verify(candidateAttachmentService).listCandidateAttachmentsByType(any(ListByUploadTypeRequest.class));
    }

    @Test
    @DisplayName("delete attachment by id succeeds")
    void deleteByIdSucceeds() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/" + CANDIDATE_ID)
                .with(csrf())
                        .header("Authorization", "Bearer " + "jwt-token"))

                .andDo(print())
                .andExpect(status().isOk());

        verify(candidateAttachmentService).deleteCandidateAttachment(CANDIDATE_ID);
    }

    private static CandidateAttachment getCandidateAttachments(boolean isCvOnly) {
        CandidateAttachment candidateAttachment = new CandidateAttachment();
        candidateAttachment.setCv(isCvOnly);
        return candidateAttachment;
    }

}
