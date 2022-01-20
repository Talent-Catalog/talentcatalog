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
  selectedTask: TaskAssignment;

  constructor() { }

  ngOnInit(): void {
    console.log(this.candidate);


  }

  filterOngoing(ta: TaskAssignment) {
    return ta.completedDate == null;
  }

  filterCompleted(ta: TaskAssignment) {
    return ta.completedDate != null;
  }

  get ongoingTasks() {
    return this.candidate?.taskAssignments.filter(t => t.completedDate == null);
  }

  get completedTasks() {
    return this.candidate?.taskAssignments.filter(t => t.completedDate != null);
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < new Date()) && !ta.task.optional;
  }

  selectTask(ta: TaskAssignment) {
    this.selectedTask = ta;
  }

  unSelectTask() {
    this.selectedTask = null;
  }

}
