/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.util.filesystem.Folder;

/**
 * Standard interface to any filesystem
 *
 * @author John Cameron
 */
public interface FileSystemService {

    /**
     * Finds a folder with the given name.
     * If there is more than one folder with that name, any one could be
     * returned.
     * @param folderName Name of folder being searched for
     * @return Null if no folder found
     */
    @Nullable
    Folder findAFolder(String folderName);

    @NonNull
    Folder createFolder(String folderName) throws IOException;
}
