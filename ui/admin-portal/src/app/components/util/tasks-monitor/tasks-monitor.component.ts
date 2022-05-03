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
