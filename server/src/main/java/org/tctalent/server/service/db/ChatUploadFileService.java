// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.service.db;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;

/**
 * Supports uploading files to a chat post.
 *
 * @author John Cameron
 */
public interface ChatUploadFileService {

    /**
     * Upload a file to a chat post which is stored in the job's Google Drive folder, in a
     * ChatUploads subfolder.
     * This file upload is to be viewable by anyone with the link so that it can be displayed on
     * the TC as part of the chat post.
     * @param id Id of the chat post
     * @param file The file to upload to the Google Drive
     * @return String URL of the file location on the Google Drive.
     * The URL isn't stored on the ChatPost object in the database, but instead it will be stored
     * as part of an <img> tag in the ChatPost content.
     * @throws NoSuchObjectException if there is no post with this id.
     * @throws IOException If there was a problem uploading the file.
     */
    String uploadFile(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException;

}
