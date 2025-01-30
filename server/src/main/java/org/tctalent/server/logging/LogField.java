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

package org.tctalent.server.logging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The {@code LogField} enum represents the fields that can be included in a log message.
 * Each field has a label and a sort order, which is used to determine the order in which
 * the fields are included in the log message.
 * <p>
 * The following are the available log fields:
 *
 * <ul>
 *     <li>CPU_UTILIZATION - CPU Utilization field</li>
 *     <li>MEMORY_UTILIZATION - Memory Utilization field</li>
 *     <li>USER_ID - User ID field</li>
 *     <li>CANDIDATE_ID - Candidate ID field</li>
 *     <li>JOB_ID - Job ID field</li>
 *     <li>LIST_ID - List ID field</li>
 *     <li>SEARCH_ID - Search ID field</li>
 *     <li>JOB_OPP_ID - Job Opportunity ID field</li>
 *     <li>CASE_ID - Candidate Opportunity ID field</li>
 *     <li>ACTION - Action field</li>
 *     <li>MESSAGE - Message field</li>
 * </ul>
 *
 * Each field has a corresponding label and sort order.
 * <p>
 * Example usage:
 * <pre>
 *     LogField.USER_ID.getLabel();  // returns "uid"
 *     LogField.USER_ID.getSortOrder();  // returns 100
 * </pre>
 *
 * @author sadatmalik
 */
@Getter
@RequiredArgsConstructor
public enum LogField {

  CPU_UTILIZATION("cpu", 98),
  MEMORY_UTILIZATION("mem", 99),
  USER_ID("uid", 100),
  CANDIDATE_ID("cid", 101),
  JOB_ID("jid", 102),
  LIST_ID("lid", 103),
  SEARCH_ID("sid", 104),
  JOB_OPP_ID("jpid", 105),
  CASE_ID("cxid", 106),
  ACTION("action", 107),
  MESSAGE("msg", 108);

  /**
   * The label of the log field, used as a prefix in log messages.
   */
  private final String label;

  /**
   * The sort order of the log field, used to determine the order of fields in log messages.
   */
  private final Integer sortOrder;
}
