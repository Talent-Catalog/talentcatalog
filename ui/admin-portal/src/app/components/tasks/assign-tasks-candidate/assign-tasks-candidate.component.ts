import {Component, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {TaskService} from "../../../services/task.service";
import {CreateTaskAssignmentRequest, TaskAssignmentService} from "../../../services/task-assignment.service";
import {TaskAssignment} from "../../../model/task-assignment";
import {Task} from "../../../model/task";

@Component({
  selector: 'app-assign-tasks-candidate',
  templateUrl: './assign-tasks-candidate.component.html',
  styleUrls: ['./assign-tasks-candidate.component.scss']
})
export class AssignTasksCandidateComponent implements OnInit {
  assignForm: FormGroup;
  candidateId: number;
  allTasks: Task[];
  loading;
  error;
  saving;
  estDate: Date;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private modalService: NgbModal,
              private taskService: TaskService,
              private taskAssignmentService: TaskAssignmentService) { }

  ngOnInit(): void {
    this.loading = true;
    this.assignForm = this.fb.group({
      task: [null],
      customDate: [false],
      dueDate: [null]
    });
    this.getAllTasks();
  }

  get selectedTask(): Task {
    return this.assignForm?.value?.task;
  }

  get estimatedDueDate() {
    this.estDate = new Date();
    this.estDate.setDate( this.estDate.getDate() + this.selectedTask.daysToComplete );
    return this.estDate;
  }

  getAllTasks() {
    this.allTasks = [];
    this.loading = true;
    this.error = null;
    this.taskService.listTasks().subscribe(
      (tasks: Task[]) => {
        this.allTasks = tasks;
        this.loading = false;
      },

      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  onSave() {
    this.saving = true;

    const task: Task = this.assignForm.value.task;

    //Pick up candidate and task
    const request: CreateTaskAssignmentRequest = {
      candidateId: this.candidateId,
      taskId: task.id,
      dueDate: this.assignForm.value.dueDate
    }

    this.taskAssignmentService.createTaskAssignment(request).subscribe(
      (taskAssignment: TaskAssignment) => {
          this.activeModal.close(taskAssignment);
          this.saving = false;
        },
      error => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  cancel() {
    this.activeModal.dismiss();
  }

  // Allow to search for either a task name or a task type.
  searchTypeOrName = (searchTerm: string, item: any) => {
    return item.taskType.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1 || item.displayName.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1;
  }

}
