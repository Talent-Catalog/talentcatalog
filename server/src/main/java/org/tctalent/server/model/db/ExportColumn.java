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

package org.tctalent.server.model.db;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.server.request.candidate.PublishedDocColumnProps;

/**
 * Represents the columns of data that are published when a particular candidate source is
 * "published".
 * <p/>
 * The published data currently ends up in a Google Sheet.
 * <p/>
 * This data reflects what is stored on the server which are just string keys describing the
 * type of column plus optional configuration, overriding some default column data.
 * This server data describes which columns are used and the order in which they appear.
 * <p/>
 * The actual detail of the column is stored in Angular on the front end.
 *
 * @author John Cameron
 */
@Entity
@Table(name = "export_column")
@SequenceGenerator(name = "seq_gen", sequenceName = "export_column_id_seq", allocationSize = 1)
@Getter
@Setter
public class ExportColumn extends AbstractDomainObject<Long> {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "saved_list_id")
  /**
   * If not null, this is the candidate source with which the column is associated.
   */
  private SavedList savedList;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "saved_search_id")
  /**
   * If not null, this is the candidate source with which the column is associated.
   */
  private SavedSearch savedSearch;

  /**
   * Defines where this column appears in the published document.
   * Index 0 is the first column.
   */
  private int index;

  /**
   * This is the unique key defining a particular column definition.
   * It should correspond to a column definition with that key defined on the Angular front end.
   * If not, it will be ignored.
   */
  private String key;

  @Convert(converter= PropertiesStringConverter.class)
  @Nullable
  /**
   * If present, these properties override some default properties of the standard column
   * definition - for example the column header text.
   */
  private PublishedDocColumnProps properties;

}
