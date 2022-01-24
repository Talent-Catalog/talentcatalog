import {Component, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Task} from "../../../model/candidate";
import {TaskService} from "../../../services/task.service";

@Component({
  selector: 'app-assign-tasks-candidate',
  templateUrl: './assign-tasks-candidate.component.html',
  styleUrls: ['./assign-tasks-candidate.component.scss']
})
export class AssignTasksCandidateComponent implements OnInit {
  assignForm: FormGroup;
  allTasks: Task[];
  loading;
  error;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private modalService: NgbModal,
              private taskService: TaskService) { }

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
      },

      error => {
        this.error = error;
      }

    );
    this.loading = false;
  }

  onSave() {
    // todo save the task and assign to the candidate returning the task assignment
    this.activeModal.close();
  }

  cancel() {
    this.activeModal.dismiss();
  }

}
