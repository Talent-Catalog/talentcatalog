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

package org.tctalent.server.service.db.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.configuration.GoogleDriveConfig;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.LinkPreview;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.chat.Post;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.repository.db.JobChatRepository;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.filesystem.GoogleFileSystemDrive;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;
import org.tctalent.server.util.filesystem.GoogleFileSystemFolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPostServiceImpl implements ChatPostService {

    private final ChatPostRepository chatPostRepository;
    private final GoogleDriveConfig googleDriveConfig;
    private final FileSystemService fileSystemService;
    private final JobChatRepository jobChatRepository;
    private final CandidateService candidateService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public ChatPost createPost(@NonNull Post post, @NonNull JobChat jobChat, User user) {
        ChatPost chatPost = new ChatPost();
        chatPost.setJobChat(jobChat);
        chatPost.setContent(post.getContent());
        chatPost.setCreatedDate(OffsetDateTime.now());
        chatPost.setCreatedBy(user);
        if (post.getLinkPreviews() != null) {
            for (LinkPreview linkPreview : post.getLinkPreviews()) {
                chatPost.addLinkPreview(linkPreview);
            }
        }

        chatPost = chatPostRepository.save(chatPost);

        return chatPost;
    }

    @Override
    @NonNull
    public ChatPost getChatPost(long id) throws NoSuchObjectException {
       return chatPostRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(ChatPost.class, id));
    }

    @Override
    public DtoBuilder getChatPostDtoBuilder() {
        return new DtoBuilder()
            .add("id")
            .add("content")
            .add("createdDate")
            .add("createdBy", userDto())
            .add("jobChat", jobChatDto())
            .add("updatedDate")
            .add("updatedBy", userDto())
            .add("reactions", reactionDto())
            .add("linkPreviews", linkPreviewDto())
            ;
    }

    private DtoBuilder linkPreviewDto() {
        return new DtoBuilder()
            .add("id")
            .add("url")
            .add("title")
            .add("description")
            .add("imageUrl")
            .add("domain")
            .add("faviconUrl")
            ;
    }

    private DtoBuilder jobChatDto() {
        return new DtoBuilder()
            .add("id")
            ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
            .add("id")
            .add("firstName")
            .add("lastName")
            .add("partner", partnerDto())
            .add("role")
            ;
    }

    private DtoBuilder partnerDto() {
        return new DtoBuilder()
            .add("id")
            .add("abbreviation")
            ;
    }

    private DtoBuilder reactionDto() {
        return new DtoBuilder()
            .add("id")
            .add("emoji")
            .add("users", reactionUserDto())
            ;
    }

    private DtoBuilder reactionUserDto() {
        return new DtoBuilder()
            .add("id")
            .add("displayName")
            ;
    }

    @Nullable
    @Override
    public ChatPost getLastChatPost(long chatId) {
        Long postId = chatPostRepository.findLastChatPost(chatId);
        ChatPost post = postId == null ? null : getChatPost(postId);
        return post;
    }

    @NonNull
    public List<ChatPost> listChatPosts(long chatId) {
        return chatPostRepository.findByJobChatIdOrderByIdAsc(chatId)
            .orElseThrow(() -> new NoSuchObjectException(JobChat.class, chatId));
    }

    @Override
    public void publishChatPost(ChatPost post) {
        final Map<String, Object> postDto = getChatPostDtoBuilder().build(post);
        messagingTemplate.convertAndSend(
            CHAT_PUBLISH_ROOT + "/" + post.getJobChat().getId(), postDto);
    }

    @Override
    public String uploadFile(long id, MultipartFile file)
        throws NoSuchObjectException, IOException {
        JobChat chat = jobChatRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(JobChat.class, id));

        GoogleFileSystemFile uploadedFile = uploadChatFile(chat, file);

        return createEmbedDisplayLink(uploadedFile, file);
    }

    private GoogleFileSystemFile uploadChatFile(JobChat chat, MultipartFile file)
        throws IOException {

        //Name of file being uploaded - prefixed with job chat id.
        String fileName = chat.getId() + "-" + file.getOriginalFilename();

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

        final GoogleFileSystemDrive drive = getGoogleDrive(chat);
        final GoogleFileSystemFolder parentFolder = new GoogleFileSystemFolder(getFolderLink(chat));

        // Store the chat post uploads to a new ChatUploads folder in the job's Google folder.
        // If it doesn't exist, create it.
        GoogleFileSystemFolder chatFolder;
        String chatUploadFolderName = "ChatUploads";
        chatFolder = fileSystemService.findAFolder(drive, parentFolder, chatUploadFolderName);
        if (chatFolder == null) {
            //No folder exists on drive, create it
            chatFolder = fileSystemService.createFolder(
                drive, parentFolder, chatUploadFolderName);
            //Make publicly viewable
            fileSystemService.publishFolder(chatFolder);
        }

        //Upload the file to its folder, with the correct name (not the temp file name).
        GoogleFileSystemFile uploadedFile =
            fileSystemService.uploadFile(drive, chatFolder, fileName, tempFile);

        //Delete tempfile
        if (!tempFile.delete()) {
            LogBuilder.builder(log)
                .action("UploadChatFile")
                .message("Failed to delete temporary file " + tempFile)
                .logError();
        }

        return uploadedFile;
    }

    /**
     * In order for the file to display via the html in the post or the editor, we need to alter
     * the link. It needs to be a display link (not embed or preview).
     * See here: <a href="https://support.google.com/drive/thread/34363118?hl=en&msgid=34384934">...</a>
     * The link type differs for an image tag embed, or if it's a hyperlink to a file.
     * Update! There is a bug/change with Google that no longer is embedding images as before. The work around is setting
     * to a thumbnail but with a param to set width UPDATE MARCH 28: thumbnail link for img tag no longer seems to work.
     * See here for workaround: <a href="https://support.google.com/sites/thread/253003338/images-from-google-drive-in-embedded-html-no-longer-working?hl=en">...</a>
     * @param uploadedFile which we want to display in the html of the post
     * @param file we uploaded that we want to get the content type from to determine which link we need.
     * @return string url to embed and open file
     */
    private String createEmbedDisplayLink(GoogleFileSystemFile uploadedFile, MultipartFile file) {
        if (file.getContentType() != null && file.getContentType().startsWith("image")) {
            return "https://drive.google.com/thumbnail?sz=w1920&id=" +  uploadedFile.getId();
        } else {
            return "https://drive.google.com/file/d/" +  uploadedFile.getId() + "/view?usp=sharing";
        }
    }

    /**
     * Get the appropriate GoogleDrive folder link to save the chat post file upload to.
     * Determined by if the chat is job related (has Job id) or is candidate opp related (has CandidateOpp id).
     * If it's job related the folder will be the job's submission list folder.
     * If it's candidate opp related, it will go in the candidate's folder.
     * And if the candidate has no folder yet, we need to create it.
     * @param chat JobChat we are uploading a file to
     * @return the appropriate folder link to save the file to.
     */
    private String getFolderLink(JobChat chat) throws NoSuchObjectException, IOException {
        String folderLink;
        if (chat.getJobOpp() != null) {
            folderLink = chat.getJobOpp().getSubmissionList().getFolderlink();
        } else if (chat.getCandidate() != null) {
            folderLink = chat.getCandidate().getFolderlink();
            // If the candidate related to the candidate opportunity has no Google Drive folder, create it.
            if (folderLink == null) {
                long candidateId = chat.getCandidate().getId();
                Candidate candidate = this.candidateService.createCandidateFolder(candidateId);
                folderLink = candidate.getFolderlink();
            }
        } else {
            throw new NoSuchObjectException("No candidate or job associated with chat.");
        }
        return folderLink;
    }

    private GoogleFileSystemDrive getGoogleDrive(JobChat chat) throws NoSuchObjectException {
        GoogleFileSystemDrive drive;
        if (chat.getJobOpp() != null) {
            drive = googleDriveConfig.getListFoldersDrive();
        } else if (chat.getCandidate() != null) {
            drive = googleDriveConfig.getCandidateDataDrive();
        } else {
            throw new NoSuchObjectException("No candidate or job associated with chat.");
        }
        return drive;
    }
}
