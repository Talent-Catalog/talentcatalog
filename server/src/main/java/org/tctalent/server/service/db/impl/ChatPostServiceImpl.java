/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
@RequiredArgsConstructor
public class ChatPostServiceImpl implements ChatPostService {

    private final UserService userService;
    private final ChatPostRepository chatPostRepository;
    private final GoogleDriveConfig googleDriveConfig;
    private final FileSystemService fileSystemService;

    private static final Logger log = LoggerFactory.getLogger(ChatPostServiceImpl.class);

    @Override
    public ChatPost createPost(@NonNull Post post, @NonNull JobChat jobChat) {
        ChatPost chatPost = new ChatPost();
        chatPost.setJobChat(jobChat);
        chatPost.setContent(post.getContent());
        chatPost.setCreatedDate(OffsetDateTime.now());
        chatPost.setCreatedBy(userService.getLoggedInUser());

        chatPost = chatPostRepository.save(chatPost);
        return chatPost;
    }

    @Override
    @NonNull
    public ChatPost getChatPost(long id) throws NoSuchObjectException {
       return chatPostRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(ChatPost.class, id));
    }

    public List<ChatPost> listChatPosts(long chatId) {
        return chatPostRepository.findByJobChatId(chatId)
            .orElseThrow(() -> new NoSuchObjectException(JobChat.class, chatId));
    }

    @Override
    public String uploadFile(long id, MultipartFile file)
        throws InvalidRequestException, NoSuchObjectException, IOException {

        ChatPost post = getChatPost(id);
        GoogleFileSystemFile uploadedFile = uploadChatFile(post, file);

        return uploadedFile.getUrl();
    }

    private GoogleFileSystemFile uploadChatFile(ChatPost chat, MultipartFile file)
        throws IOException {
        SalesforceJobOpp job = chat.getJobChat().getJobOpp();

        String folderLink = job.getSubmissionList().getFolderlink();

        //Name of file being uploaded - prefixed with job chat id.
        String fileName = chat.getId() + '_' + file.getOriginalFilename();

        return uploadFile(folderLink, fileName, file);
    }

    private GoogleFileSystemFile uploadFile(String folderLink, String fileName,
        MultipartFile file) throws IOException {

        //Save to a temporary file
        InputStream is = file.getInputStream();
        File tempFile = File.createTempFile("job", ".tmp");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

        final GoogleFileSystemDrive listFoldersDrive = googleDriveConfig.getListFoldersDrive();
        final GoogleFileSystemFolder parentFolder = new GoogleFileSystemFolder(folderLink);

        // Store the chat post uploads to a new ChatUploads folder in the job's Google folder.
        // If it doesn't exist, create it.
        GoogleFileSystemFolder chatFolder;
        String chatUploadFolderName = "ChatUploads";
        chatFolder = fileSystemService.findAFolder(listFoldersDrive, parentFolder, chatUploadFolderName);
        if (chatFolder == null) {
            //No folder exists on drive, create it
            chatFolder = fileSystemService.createFolder(
                listFoldersDrive, parentFolder, chatUploadFolderName);
        }

        //Upload the file to its folder, with the correct name (not the temp file name).
        GoogleFileSystemFile uploadedFile =
            fileSystemService.uploadFile(listFoldersDrive, chatFolder, fileName, tempFile);

        // Publish the file so that it can be viewed in the TC by anyone with the link
        fileSystemService.publishFile(uploadedFile);

        //Delete tempfile
        if (!tempFile.delete()) {
            log.error("Failed to delete temporary file " + tempFile);
        }

        return uploadedFile;
    }
}
