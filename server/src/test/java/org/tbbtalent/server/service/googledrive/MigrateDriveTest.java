package org.tbbtalent.server.service.googledrive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.service.db.impl.CandidateAttachmentsServiceImpl;
import org.tbbtalent.server.service.db.impl.GoogleFileSystemServiceImpl;
import org.tbbtalent.server.service.db.impl.UserServiceImpl;
import org.tbbtalent.server.util.filesystem.FileSystemFolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MigrateDriveTest {

    @Value("${google.drive.candidateDataDriveId}")
    private String candidateDataDriveId;

    @Value("${google.drive.candidateRootFolderId}")
    private String candidateRootFolderId;

    @Autowired
    private Drive googleDriveService;

    @Autowired
    private GoogleFileSystemServiceImpl googleFileSystemService;

    @Transactional
    @Test
    void getSampleGoogleFolders() throws IOException {
        FileList result = googleDriveService.files().list()
                .setQ("'" + candidateRootFolderId + "' in parents" +
                        " and mimeType='application/vnd.google-apps.folder'")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setCorpora("drive")
                .setDriveId(candidateDataDriveId)
                .setFields("nextPageToken, files(id,name,webViewLink)")
                .execute();
        List<File> folders = result.getFiles();
        assertNotNull(folders);

        FileSystemFolder folder = googleFileSystemService.findAFolder("42772");
        assertNotNull(folder);
    }

}
