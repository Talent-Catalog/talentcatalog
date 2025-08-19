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

import {Task} from "./task";
import {toDateOnly} from "../util/date-adapter/date-adapter";
import {Status} from "./base";

export interface TaskAssignment {
  id: number;
  abandonedDate: Date;
  candidateNotes: string;
  completedDate: Date;
  dueDate: Date;
  status: Status;
  task: Task;
  answer: string;
  candidateProperties?: { name: string; value: string }[];
}

/**
 * Defines standard sort for Task Assignments.
 * <p/>
 * Basically ongoing task assignments, come before completed (or abandoned) ones.
 * Then ongoing task assignments are sorted by due date (ascending order), and then alphabetically
 * for a given due date.
 * Completed (or abandoned) task assignments are just sorted alphabetically by task name.
 * @param a Task assignment
 * @param b Another task assignment
 * @return 1, 0 or -1 for "a" greater, equal or less than "b"
 */
export function taskAssignmentSort(a: TaskAssignment, b: TaskAssignment) {
  function isOngoingTaskAssignment(ta: TaskAssignment) {
    return ta.completedDate == null && ta.abandonedDate == null;
  }

  if (!isOngoingTaskAssignment(a) && !isOngoingTaskAssignment(b)) {
    //Neither task assignment is ongoing, just sort by task name
    return a.task.displayName.localeCompare(b.task.displayName);
  } else if (isOngoingTaskAssignment(a) && isOngoingTaskAssignment(b)) {
    //Both task assignments are ongoing, sort by due date, then task name
    //Strip any time off dueDate
    const aDateOnly = toDateOnly(a.dueDate);
    const bDateOnly = toDateOnly(b.dueDate);
    if (aDateOnly.getTime() === bDateOnly.getTime()) {
      //Dates are the same, sort by task name
      return a.task.displayName.localeCompare(b.task.displayName)
    } else {
      //Dates are different, sort by date - most recent first
      return aDateOnly > bDateOnly ? 1 : -1;
    }
  } else {
    //One is ongoing the other is not. The ongoing one comes first
    return isOngoingTaskAssignment(a) ? 1 : -1;
  }
}

export function checkForOverdue(values: TaskAssignment[]) {
  return values.some(ta =>
    new Date(ta.dueDate) < new Date() &&
    (!ta.abandonedDate && !ta.completedDate && !ta.task.optional)
  );
}

export function checkForAbandoned(values: TaskAssignment[]) {
  return values.some(ta => ta.abandonedDate && (!ta.completedDate && !ta.task.optional));
}

export function checkForCompleted(values: TaskAssignment[]) {
  return values.some(ta => ta.completedDate);
}

export function checkForOngoing(values: TaskAssignment[]) {
  return values.some(ta => new Date(ta.dueDate) > new Date() && (!ta.abandonedDate && !ta.completedDate));
}
