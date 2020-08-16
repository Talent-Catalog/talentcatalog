/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.util.filesystem;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Base class for representing folders or files on a remote file system
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public abstract class FileSystemBaseEntity {
    private String name;
    
    @NonNull
    private String id;
    
    private String url;
}
