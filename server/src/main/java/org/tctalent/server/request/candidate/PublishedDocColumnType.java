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

/**
 * Published columns can for display purposes only, or they can allow different kinds of data to be
 * entered in them (eg by employers) which we can process, interpreting the data depending on its
 * type.
 * This entered data may get imported back into the Talent Catalog data base or into Salesforce.
 * @author John Cameron
 */
public enum PublishedDocColumnType {
  /**
   * Column is only used to display data.
   * <p/>
   * Other types of column are used for entering data which can be subsequently imported.
   */
  DisplayOnly,

  /**
   * Employers should enter their feedback about candidates into the column.
   */
  EmployerCandidateNotes,

  /**
   * Employers should enter their candidate hiring decision into the column.
   */
  EmployerCandidateDecision,

  /**
   * A universal use Yes/No dropdown column for data input. Could be input from employers or team members.
   */
  YesNoDropdown
}
