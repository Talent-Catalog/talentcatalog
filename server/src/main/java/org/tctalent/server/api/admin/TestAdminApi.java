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

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple test endpoint to verify the server is running correctly.
 * This endpoint is used for development and testing purposes.
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestAdminApi {

    @GetMapping
    public Map<String, Object> test() {
        log.info("Test endpoint accessed");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Talent Catalog server is running!");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "2.3.0");
        
        return response;
    }
}
