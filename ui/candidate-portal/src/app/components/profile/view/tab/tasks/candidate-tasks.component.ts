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

  displayTaskType(ta: TaskAssignment): string {
    let taskType: string;
    if (ta.task.taskType === 'Upload') {
      taskType = "Upload";
    } else if (ta.task.taskType === 'Question' || ta.task.taskType === 'YesNoQuestion') {
      taskType = "Question";
    } else {
      taskType = null;
    }
    return taskType;
  }
}
