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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Google's file system introduces the "id" - which can be extracted from the url.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class GoogleFileSystemBaseEntity extends FileSystemBaseEntity {

  /**
   * Creates an object representing a file on Google Drive.
   * <p/>
   * Google files are identified by an id - from which the url is constructed.
   * If the Google url is not specified in the constructor (ie it is null), the id must be
   * set using {@link #setId} before the object is used.
   * @param url Google url of file. If null, then setId must be called to identify the file on
   *            Google.
   */
  public GoogleFileSystemBaseEntity(@Nullable String url) {
    super(url);
  }

  private String id;

  /**
   * Extracts a Google id from the Google url (for the file or folder).
   * <p/>
   * For example, in this url for a Google folder
   * https://drive.google.com/drive/folders/1GtuMI7IjIXzL68U9OjnO5PZccJ_x7GHr?usp=sharing
   * the id is 1GtuMI7IjIXzL68U9OjnO5PZccJ_x7GHr
   * @param url Link to a Google file or folder
   * @return Google id
   */
  private static String extractIdFromUrl(String url) {
    if (url == null) {
      return null;
    }

    //See https://stackoverflow.com/questions/16840038/easiest-way-to-get-file-id-from-url-on-google-apps-script
    String pattern = ".*[^-\\w]([-\\w]{25,})[^-\\w]?.*";
    Pattern r = Pattern.compile(pattern);

    Matcher m = r.matcher(url);
    if (m.find() && m.groupCount() == 1) {
      return m.group(1);
    } else {
      return null;
    }
  }

  @NonNull
  public String getId() {
    if (id == null) {
      setId(extractIdFromUrl(getUrl()));
    }
    return id;
  }
}
