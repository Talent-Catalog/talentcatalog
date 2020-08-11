/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.util.filesystem;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a folder on a remote file system
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class Folder {
    private String name;
    private String id;
    private String url;
}
