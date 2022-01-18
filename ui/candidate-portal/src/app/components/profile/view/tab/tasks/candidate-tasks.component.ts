import {Component, Input, OnInit} from '@angular/core';
import {Candidate, TaskAssignment} from "../../../../../model/candidate";

@Component({
  selector: 'app-candidate-tasks',
  templateUrl: './candidate-tasks.component.html',
  styleUrls: ['./candidate-tasks.component.scss']
})
export class CandidateTasksComponent implements OnInit {

  error;
  loading;
  @Input() candidate: Candidate;
  ongoingTasks: TaskAssignment[];
  completedTasks: TaskAssignment[];

  constructor() { }

  ngOnInit(): void {
    this.ongoingTasks = this.candidate.taskAssignments.filter(t => t.completedDate == null);
    this.completedTasks = this.candidate.taskAssignments.filter(t => t.completedDate != null);
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < new Date()) && !ta.task.optional;
  }

}
