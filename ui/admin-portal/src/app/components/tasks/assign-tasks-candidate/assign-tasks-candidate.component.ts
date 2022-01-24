import {Component, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Task, TaskAssignment} from "../../../model/candidate";
import {TaskService} from "../../../services/task.service";
import {
  CreateTaskAssignmentRequest,
  TaskAssignmentService
} from "../../../services/task-assignment.service";

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

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private modalService: NgbModal,
              private taskService: TaskService,
              private taskAssignmentService: TaskAssignmentService) { }

  ngOnInit(): void {
    this.loading = true;
    this.assignForm = this.fb.group({
      task: [null],
      dueDate: [null]
    });
    this.getAllTasks();
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
    this.loading = true;

    const task: Task = this.assignForm.value.task;

    //Pick up candidate and task
    const request: CreateTaskAssignmentRequest = {
      candidateId: this.candidateId,
      taskId: task.id
    }

    this.taskAssignmentService.createTaskAssignment(request).subscribe(
      (taskAssignment: TaskAssignment) => {
          this.activeModal.close(taskAssignment);
          this.loading = false;
        },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  cancel() {
    this.activeModal.dismiss();
  }

}
