import {Component, Input, OnInit} from '@angular/core';
import {Candidate, Status} from "../../../model/candidate";
import {checkForAbandoned, checkForOverdue, TaskAssignment} from "../../../model/task-assignment";

@Component({
  selector: 'app-tasks-monitor',
  templateUrl: './tasks-monitor.component.html',
  styleUrls: ['./tasks-monitor.component.scss']
})
export class TasksMonitorComponent implements OnInit {
  // Required Input
  @Input() candidate: Candidate;
  @Input() completed: TaskAssignment[];
  @Input() total: TaskAssignment[];
  activeTaskAssignments: TaskAssignment[];

  hasOverdue: boolean;
  hasAbandoned: boolean;
  hasCompleted: boolean;

  constructor() { }

  ngOnInit(): void {
    this.activeTaskAssignments = this.candidate.taskAssignments.filter(ta => ta.status === Status.active);
    this.hasOverdue = checkForOverdue(this.activeTaskAssignments);
    this.hasAbandoned = checkForAbandoned(this.activeTaskAssignments);
    // Want to show green for all completed (only if there isn't any abandoned tasks also)
    this.hasCompleted = this.completed.length === this.total.length && !checkForAbandoned(this.activeTaskAssignments);
  }

}
