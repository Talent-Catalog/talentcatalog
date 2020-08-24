/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

/**
 * Access to a Google Drive file system.
 *
 * @author John Cameron
 */
public interface GoogleFileSystemService extends FileSystemService {

    /**
     * Extracts a Google id from the Google url (for the file or folder).
     * <p/>
     * For example, in this url for a Google folder
     * https://drive.google.com/drive/folders/1GtuMI7IjIXzL68U9OjnO5PZccJ_x7GHr?usp=sharing
     * the id is 1GtuMI7IjIXzL68U9OjnO5PZccJ_x7GHr 
     * @param url Link to a Google file or folder
     * @return Google id
     */
    String extractIdFromUrl(String url);
}
