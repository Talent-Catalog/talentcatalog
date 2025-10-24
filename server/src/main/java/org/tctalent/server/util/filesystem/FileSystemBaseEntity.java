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

package org.tctalent.server.util.filesystem;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;


/**
 * Base class for representing drives, folders or files on a remote file system where
 * a url can identify all.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public abstract class FileSystemBaseEntity {

    public FileSystemBaseEntity(@NonNull String url) {
        this.url = url;
    }

    /**
     * Name of the file or folder on the file system.
     * <p/>
     * Note that this is changeable (eg can be renamed to anything - manually on Google by the user)
     * and does not have to be unique. It is the url that ultimately
     * defines the file/folder.
     */
    private String name;

    /**
     * Url by which folder or file can be identified on the file system.
     */
    @NonNull
    private String url;
}
