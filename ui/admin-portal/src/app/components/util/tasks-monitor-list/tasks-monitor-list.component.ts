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

import {Component, Input, OnInit} from '@angular/core';
import {TaskAssignment} from "../../../model/task-assignment";
import {Task} from "../../../model/task";
import {SavedList} from "../../../model/saved-list";
import {TaskAssignmentService, TaskListRequest} from "../../../services/task-assignment.service";

@Component({
  selector: 'app-tasks-monitor-list',
  templateUrl: './tasks-monitor-list.component.html',
  styleUrls: ['./tasks-monitor-list.component.scss']
})
export class TasksMonitorListComponent implements OnInit {
// Required Input
  @Input() task: Task;
  @Input() list: SavedList;
  error: any;

  taskAssignments: TaskAssignment[];
  completed: TaskAssignment[];
  abandoned: TaskAssignment[];
  outstandingNotOverdue: TaskAssignment[];
  outstandingOverdue: TaskAssignment[];

  constructor(private taskAssignmentService: TaskAssignmentService) { }

  // This component displays the number of completed/abandoned/outstanding task assignments in the list for each task.
  ngOnInit(): void {
    let request: TaskListRequest = {
      taskId: this.task.id,
      savedListId: this.list.id
    }
    this.taskAssignmentService.search(request).subscribe(
      (response) => {
        this.taskAssignments = response;
        this.completed = this.taskAssignments?.filter(ta => ta.completedDate != null);
        this.abandoned = this.taskAssignments?.filter(ta => ta.abandonedDate != null);
        this.outstandingNotOverdue = this.taskAssignments?.filter(ta => new Date(ta.dueDate) > new Date() && (!ta.abandonedDate && !ta.completedDate));
        this.outstandingOverdue = this.taskAssignments?.filter(ta => new Date(ta.dueDate) < new Date() && (!ta.abandonedDate && !ta.completedDate));
      },
      error => {
        this.error = error;
      })

  }
}
