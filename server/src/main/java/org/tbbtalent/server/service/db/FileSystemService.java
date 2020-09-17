/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.util.filesystem.FileSystemFile;
import org.tbbtalent.server.util.filesystem.FileSystemFolder;

/**
 * Standard interface to any filesystem - tailored to Talent Catalog's use for
 * storing candidate data - in particular uploaded files (eg CVs)
 *
 * @author John Cameron
 */
public interface FileSystemService {

    /**
     * Finds a folder with the given name.
     * If there is more than one folder with that name, any one could be
     * returned.
     * @param folderName Name of folder being searched for
     * @return Found folder, null if no folder found
     * @throws IOException If problem accessing file system
     */
    @Nullable
    FileSystemFolder findAFolder(String folderName) throws IOException;

    /**
     * Creates a folder with the given name.
     * Does not check if folder with that name already exists - may create
     * create a duplicate folder with the same name if the file system allows it. 
     * @param folderName Name of folder to be created
     * @return Folder created
     * @throws IOException If there was a problem creating the folder
     */
    @NonNull
    FileSystemFolder createFolder(String folderName) throws IOException;

    /**
     * Deletes the given file
     * @param file Describes file to be deleted
     * @throws IOException If there was a problem deleting the file
     */
    void deleteFile(FileSystemFile file) throws IOException;

    /**
     * Downloads the given file into the given OutputStream
     * @param file Describes file to be downloaded
     * @param out Stream to write the contents of the file to
     * @throws IOException If there was a problem uploading the file.
     */
    void downloadFile(@NonNull FileSystemFile file, @NonNull OutputStream out)
            throws IOException;

    /**
     * Renames the given file 
     * @param file Description of file, including id or url, plus its new name
     * @throws IOException If there was a problem renaming the file
     */
    void renameFile(@NonNull FileSystemFile file) throws IOException;
    
    /**
     * Uploads the given local file.
     * @param parentFolder Folder that file should be uploaded to. If null, 
     *                     file is uploaded to root.
     * @param fileName Name assigned to uploaded file
     * @param file Local file to be uploaded
     * @return Info about uploaded file on the remote file system 
     * @throws IOException If there was a problem uploading the file.
     */
    @NonNull
    FileSystemFile uploadFile(
            @Nullable FileSystemFolder parentFolder, String fileName, File file) 
            throws IOException;
    
}
