package org.tbbtalent.server.service.googledrive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.service.db.impl.GoogleFileSystemServiceImpl;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// @SpringBootTest
public class MigrateDriveTest {

    private static final Logger log = LoggerFactory.getLogger(MigrateDriveTest.class);

    @Value("${google.drive.candidateDataDriveId}")
    private String candidateDataDriveId;

    @Value("${google.drive.candidateRootFolderId}")
    private String candidateRootFolderId;

    @Autowired
    private Drive googleDriveService;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleFileSystemServiceImpl googleFileSystemService;

    private List<File> folders;
    private Candidate candidate;

//    @Transactional
//    @Test
//    @BeforeEach
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
        folders = result.getFiles();
    }

//    @Test
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
