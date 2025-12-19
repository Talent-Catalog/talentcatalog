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

package org.tctalent.server.service.googledrive;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.service.db.impl.GoogleFileSystemServiceImpl;


@Tag("skip-test-in-gradle-build")
@SpringBootTest
@Slf4j
public class MigrateDriveTest {

    @Autowired
    private GoogleDriveConfig googleDriveConfig;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleFileSystemServiceImpl googleFileSystemService;

    private List<File> folders;
    private Candidate candidate;

    @Transactional
    @Test
    @BeforeEach
    void getSampleGoogleFolders() throws IOException, GeneralSecurityException {
        FileList result = googleDriveConfig.getGoogleDriveService().files().list()
                .setQ("'" + googleDriveConfig.getCandidateRootFolderId() + "' in parents" +
                        " and mimeType='application/vnd.google-apps.folder'")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setCorpora("drive")
                .setDriveId(googleDriveConfig.getCandidateDataDriveId())
                .setFields("nextPageToken, files(id,name,webViewLink)")
                .execute();
        folders = result.getFiles();
    }

    @Test
    void loopFoldersGetLocation() {
        for(File folder: folders) {
            setCandidateFolderLink(folder);
        }
    }

    void setCandidateFolderLink(File folder) {
        // Get candidate number from folder name
        String cn = checkForCN(folder.getName());
        // Find candidate with that candidate number
        if(cn != null){
            candidate = getCandidateFromCN(cn);
            if(candidate != null){
                candidate.setFolderlink(folder.getWebViewLink());
                candidateRepository.save(candidate);
            } else {
                log.error("Can't find candidate with candidate number: " + cn);
            }
        }
    }

    Candidate getCandidateFromCN(String cn) {
        Candidate candidate = candidateRepository.findByCandidateNumber(cn);
        return candidate;
    }

    String checkForCN(String folderName) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(folderName);
        if (m.find()) {
            return m.group();
        } else {
            return "";
        }
    }

}
