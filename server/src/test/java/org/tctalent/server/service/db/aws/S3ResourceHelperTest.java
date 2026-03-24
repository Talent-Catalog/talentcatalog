/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.service.db.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
@Slf4j
class S3ResourceHelperTest {
    @Autowired
    private S3ResourceHelper s3ResourceHelper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testDownload() throws FileUploadException {
        assertNotNull(s3ResourceHelper);

        File file = new File("src/test/resources/text/EnglishPdf.pdf");
        assertTrue(file.exists());

        long fileLength = file.length();

        s3ResourceHelper.uploadFile(file,"test/EnglishPdf.pdf", "text/pdf");

        File fileDload = s3ResourceHelper.downloadFile("test/EnglishPdf.pdf");

        String path = fileDload.getAbsolutePath();

        System.out.println(path);
        System.out.println("File length: " + fileDload.length() + " bytes");

        assertEquals(fileLength, fileDload.length());
    }
}
