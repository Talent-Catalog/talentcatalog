/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@SpringBootTest
@AutoConfigureMockMvc
class SavedListAdminApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnCreatedList() throws Exception {
        
        this.mockMvc.perform(
                post("/api/admin/saved-list")
                        .param("name","TestList")
                        .param("fixed", "false")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestList"))
                .andExpect(jsonPath("$.fixed").value("false"))
                .andExpect(jsonPath("$.id").isNumber());
    }
    
}