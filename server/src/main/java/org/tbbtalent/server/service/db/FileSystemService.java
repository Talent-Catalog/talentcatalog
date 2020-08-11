/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.util.filesystem.Folder;

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
    Folder findAFolder(String folderName) throws IOException;

    /**
     * Creates a folder with the given name.
     * Does not check if folder with that name already exists - may create
     * create a duplicate folder with the same name if the file system allows it. 
     * @param folderName Name of folder to be created
     * @return Folder created
     * @throws IOException If there was a problem creating the folder
     */
    @NonNull
    Folder createFolder(String folderName) throws IOException;
}
