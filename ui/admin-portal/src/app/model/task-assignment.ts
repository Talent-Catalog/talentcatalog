/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

import {Status} from "./candidate";
import {Task} from "./task";

export interface TaskAssignment {
  id: number;
  abandonedDate: Date;
  candidateNotes: string;
  completedDate: Date;
  dueDate: Date;
  status: Status;
  task: Task;
}

export function checkForOverdue(values: TaskAssignment[]) {
  return values.some(ta =>
    new Date(ta.dueDate) < new Date() &&
    (!ta.abandonedDate && !ta.completedDate && !ta.task.optional)
  );
}

export function checkForAbandoned(values: TaskAssignment[]) {
  return values.some(ta => ta.abandonedDate && !ta.completedDate);
}

export function checkForCompleted(values: TaskAssignment[]) {
  return values.some(ta => ta.completedDate);
}

export function checkForOngoing(values: TaskAssignment[]) {
  return values.some(ta => new Date(ta.dueDate) > new Date() && (!ta.abandonedDate && !ta.completedDate));
}
