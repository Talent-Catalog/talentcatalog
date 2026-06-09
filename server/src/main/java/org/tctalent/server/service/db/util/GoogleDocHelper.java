/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.util;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.exception.CvGenerationException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

/**
 * Service for generating candidate CVs as native Google Docs.
 *
 * <p>This helper follows the same CV generation structure as the existing PDF and DOCX helpers.
 * It reuses {@link DocxHelper} to generate the existing DOCX version of the CV, then delegates the
 * Google Drive upload/conversion work to {@link FileSystemService}.</p>
 *
 * <p>The returned {@link Resource} contains the created Google Doc URL as UTF-8 text. This keeps the
 * existing {@code CandidateService#generateCv(...)} flow and the existing CV download endpoint
 * reusable for the Google Doc format.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDocHelper {

  private static final String DOCX_MIME_TYPE =
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

  private static final String GOOGLE_DOC_MIME_TYPE =
      "application/vnd.google-apps.document";

  private final DocxHelper docxHelper;
  private final FileSystemService fileSystemService;
  private final GoogleDriveConfig googleDriveConfig;

  /**
   * Generates a candidate CV as a native Google Doc.
   *
   * <p>The generation flow is:</p>
   *
   * <ol>
   *   <li>Generate the existing DOCX CV using {@link DocxHelper}.</li>
   *   <li>Upload the DOCX to Google Drive.</li>
   *   <li>Ask Google Drive to convert the uploaded DOCX into a native Google Doc.</li>
   *   <li>Publish the created Google Doc.</li>
   *   <li>Return the Google Doc URL as a text {@link Resource}.</li>
   * </ol>
   *
   * @param candidate candidate whose CV should be generated
   * @param showName whether the candidate name should be included in the CV
   * @param showContact whether contact details should be included in the CV
   * @return text resource containing the created Google Doc URL
   * @throws CvGenerationException if DOCX generation, upload, conversion, or publishing fails
   */
  public Resource generateGoogleDoc(Candidate candidate, Boolean showName, Boolean showContact) {
    try {
      Resource docx = docxHelper.generateDocx(candidate, showName, showContact);

      GoogleFileSystemFolder parentFolder = resolveGoogleDocExportFolder(candidate);
      GoogleFileSystemDrive drive = fileSystemService.getDriveFromEntity(parentFolder);

      GoogleFileSystemFile googleDoc = fileSystemService.uploadFileWithConversion(
          drive,
          parentFolder,
          buildGoogleDocName(candidate),
          docx,
          DOCX_MIME_TYPE,
          GOOGLE_DOC_MIME_TYPE
      );

      fileSystemService.publishFile(googleDoc);

      return new ByteArrayResource(googleDoc.getUrl().getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      LogBuilder.builder(log)
          .candidateId(candidate.getId())
          .action("generateGoogleDoc")
          .message("Error generating Google Doc CV")
          .logError(e);

      throw new CvGenerationException(e.getMessage());
    }
  }

  /**
   * Resolves the Google Drive folder where the generated Google Doc CV should be created.
   *
   * <p>If the candidate already has a Google Drive folder link, the Google Doc is created in that
   * folder. Otherwise, it is created in the configured CandidateData root folder.</p>
   *
   * @param candidate candidate whose CV is being generated
   * @return Google Drive folder where the generated Google Doc should be created
   */
  private GoogleFileSystemFolder resolveGoogleDocExportFolder(Candidate candidate) {
    if (StringUtils.isNotBlank(candidate.getFolderlink())) {
      GoogleFileSystemFolder candidateFolder =
          new GoogleFileSystemFolder(candidate.getFolderlink());

      if (StringUtils.isNotBlank(candidateFolder.getId())) {
        return candidateFolder;
      }
    }

    return googleDriveConfig.getCandidateRootFolder();
  }

  /**
   * Builds a safe file name for the generated Google Doc CV.
   *
   * @param candidate candidate whose CV is being generated
   * @return safe Google Doc file name
   */
  private String buildGoogleDocName(Candidate candidate) {
    String displayName = candidate.getUser() == null
        ? null
        : candidate.getUser().getDisplayName();

    String baseName = StringUtils.defaultIfBlank(
        displayName,
        "Candidate-" + candidate.getId()
    );

    return baseName.replaceAll("[\\\\/:*?\"<>|]", "-") + "-CV";
  }
}