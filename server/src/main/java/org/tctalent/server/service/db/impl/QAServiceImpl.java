/*
 * Copyright (c) 2025 Talent Beyond Boundaries.
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

package org.tctalent.server.service.db.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.tctalent.server.service.db.QAService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Implementation of QAService that loads QA content from a JSON file.
 */
@Service
@Slf4j
public class QAServiceImpl implements QAService {

    private String cachedQAContext;
    private static final String QA_FILE_PATH = "static/pdf/chatbotQAFile.json";

    /**
     * Loads and parses the QA file on service initialization.
     */
    @PostConstruct
    public void initialize() {
        loadQAContext();
    }

    @Override
    public String loadQAContext() {
        if (cachedQAContext != null) {
            return cachedQAContext;
        }

        try {
            ClassPathResource resource = new ClassPathResource(QA_FILE_PATH);
            InputStream inputStream = resource.getInputStream();
            
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> qaList = objectMapper.readValue(
                inputStream,
                new TypeReference<List<Map<String, String>>>() {}
            );

            StringBuilder sb = new StringBuilder();
            sb.append("Here are the FAQs about Talent Beyond Boundaries (TBB) and labour mobility:\n\n");
            
            for (Map<String, String> qa : qaList) {
                String question = qa.get("question");
                String answer = qa.get("answer");
                sb.append("Q: ").append(question).append("\n");
                sb.append("A: ").append(answer).append("\n\n");
            }
            
            cachedQAContext = sb.toString();
            log.info("Successfully loaded {} QA pairs from {}", qaList.size(), QA_FILE_PATH);
            
            return cachedQAContext;
            
        } catch (IOException e) {
            log.error("Error loading QA file from {}", QA_FILE_PATH, e);
            return "";
        }
    }
}

