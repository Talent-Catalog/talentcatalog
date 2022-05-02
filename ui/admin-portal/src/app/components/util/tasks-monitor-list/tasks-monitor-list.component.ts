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

  hasOverdue: boolean;
  hasAbandoned: boolean;
  hasCompleted: boolean;
  taskAssignments: TaskAssignment[];
  completed: TaskAssignment[];
  abandoned: TaskAssignment[];
  outstanding: TaskAssignment[];

  constructor(private taskAssignmentService: TaskAssignmentService) { }
  // GET all task assignments for a particular task belonging to a list.
  // All the task assignments belonging to list, and then filter by task type
  // SHOW TOTAL INCOMPLETE

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
        this.outstanding = this.taskAssignments?.filter(ta => ta.completedDate == null && ta.abandonedDate == null);
      },
      error => {
        this.error = error;
      })


  }
}
