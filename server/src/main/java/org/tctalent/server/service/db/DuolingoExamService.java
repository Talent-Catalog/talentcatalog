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

package org.tctalent.server.service.db;

import org.tctalent.server.exception.NoSuchObjectException;

/**
 * Service interface for handling Duolingo exam related operations.
 */
public interface DuolingoExamService {

  /**
   * Updates the candidate exams based on results from the Duolingo dashboard.
   * Scheduled task that updates candidate exams for Duolingo on a daily basis.
   * This method is executed once a day at midnight GMT (00:00:00), and is locked using the
   * SchedulerLock annotation to ensure that only one instance of the task is running at any
   * given time. The lock is held for at least 23 hours and at most 23 hours to prevent overlapping
   * executions if the task takes longer than expected.
   * The task executes the updateCandidateExams method from the duolingoExamService to perform
   * the necessary updates on candidate exams.
   * @throws NoSuchObjectException if no candidate is found or any other related errors occur
   */
  void updateCandidateExams() throws NoSuchObjectException;
}
