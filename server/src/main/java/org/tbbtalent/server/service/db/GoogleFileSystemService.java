/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
