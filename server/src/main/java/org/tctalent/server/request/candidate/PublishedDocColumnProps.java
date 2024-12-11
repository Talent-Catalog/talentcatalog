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

package org.tctalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * These properties, if present, override some default values in a column definition.
 * <p/>
 * For example, the standard column header can be replaced.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class PublishedDocColumnProps {

  /**
   * If present, supplies a non default header for the column
   */
  @Nullable
  private String header;

  /**
   * If present, supplies a non default constant value to appear in the column.
   * <p/>
   * Note that if the column definition specifies that the value of a candidate field should
   * appear in the column, rather than a constant value, then this property is ignored.
   */
  @Nullable
  private String constant;

  //These constants are used to convert this object to and from a string representation, which is
  //how it is store in the database.
  private final static String DELIMITER = "\t";
  private final static String PROP_VALUE_DELIMITER = "=";
  private final static String PROP_HEADER_NAME = "header";
  private final static String PROP_CONSTANT_NAME = "constant";

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (header != null) {
      sb.append(PROP_HEADER_NAME).append(PROP_VALUE_DELIMITER).append(header);
      if (constant != null) {
        sb.append(DELIMITER);
      }
    }
    if (constant != null) {
      sb.append(PROP_CONSTANT_NAME).append(PROP_VALUE_DELIMITER).append(constant);
    }
    return  sb.toString();
  }

  /**
   * Construct this properties object from a String representation, as defined by
   * {@link #toString()}. This is used to construct the object from its string representation
   * in the database.
   * @param s String representation - if null, the individual properties are left null.
   */
  public PublishedDocColumnProps(@Nullable String s) {
    if (s != null) {
      String[] props = s.split(DELIMITER);
      for (String prop : props) {
        String[] nv = prop.split(PROP_VALUE_DELIMITER);
        if (nv.length > 0) {
          String val = nv.length == 1 ? "" : nv[1];
          switch (nv[0]) {
            case PROP_HEADER_NAME:
              header = val;
              break;
            case PROP_CONSTANT_NAME:
              constant = val;
              break;
          }
        }
      }
    }
  }

  public PublishedDocColumnProps() {
    this(null);
  }

  /*
     Implemented the following hashCode and equals to get rid of following startup warning from
     Spring. Don't really understand it since class should inherit standard Object implementations
     anyway - which is all I am hooking into here.

     John Cameron

     Spring giving this error:
     Encountered Java type [class candidate.request.org.tctalent.server.PublishedDocColumnProps] for
     which we could not locate a JavaTypeDescriptor and which does not appear to
     implement equals and/or hashCode.  This can lead to significant performance problems when
     performing equality/dirty checking involving this Java type.
     Consider registering a custom JavaTypeDescriptor or at least implementing equals/hashCode.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
}
