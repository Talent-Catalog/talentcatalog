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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {TaskAssignment, taskAssignmentSort} from "../../../../../model/task-assignment";
import {Status} from "../../../../../model/base";

@Component({
  selector: 'app-candidate-tasks',
  templateUrl: './candidate-tasks.component.html',
  styleUrls: ['./candidate-tasks.component.scss']
})
export class CandidateTasksComponent implements OnInit {

  error;
  loading;
  @Input() candidate: Candidate;
  @Output() refresh = new EventEmitter();
  selectedTask: TaskAssignment;

  constructor() { }

  ngOnInit(): void {
  }

  get ongoingTasks(): TaskAssignment[] {
    const filter = this.candidate?.taskAssignments.filter(t =>
      t.completedDate == null && t.abandonedDate == null && t.status === Status.active);
    return filter ? filter.sort(taskAssignmentSort) : filter;
  }

  get completedOrAbandonedTasks(): TaskAssignment[] {
    const filter: TaskAssignment[] = this.candidate?.taskAssignments.filter(t =>
      (t.completedDate != null || t.abandonedDate != null) && t.status === Status.active);
    return filter ? filter.sort(taskAssignmentSort) : filter;
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < new Date()) && !ta.task.optional;
  }

  selectTask(ta: TaskAssignment) {
    this.selectedTask = ta;
  }

  unSelectTask() {
    this.selectedTask = null;
    this.refresh.emit();
  }
}
