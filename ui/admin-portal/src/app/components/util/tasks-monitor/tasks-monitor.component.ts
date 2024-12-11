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
import {Candidate} from "../../../model/candidate";
import {checkForAbandoned, checkForOverdue, TaskAssignment} from "../../../model/task-assignment";

@Component({
  selector: 'app-tasks-monitor',
  templateUrl: './tasks-monitor.component.html',
  styleUrls: ['./tasks-monitor.component.scss']
})
export class TasksMonitorComponent implements OnInit {
  // Required Input
  @Input() candidate: Candidate;
  @Input() completedTasks: TaskAssignment[];
  @Input() totalTasks: TaskAssignment[];

  hasOverdue: boolean;
  hasAbandoned: boolean;
  hasCompleted: boolean;

  constructor() { }

  ngOnInit(): void {
    this.hasOverdue = checkForOverdue(this.totalTasks);
    this.hasAbandoned = checkForAbandoned(this.totalTasks);
    // Only show the monitor if there are incomplete tasks, if hasCompleted is true then hide.
    this.hasCompleted = this.completedTasks.length === this.totalTasks.length && !checkForAbandoned(this.totalTasks);
  }

}
