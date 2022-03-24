import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {TaskService} from "../../../../services/task.service";
import {Task} from "../../../../model/task";

@Component({
  selector: 'app-edit-task',
  templateUrl: './edit-task.component.html',
  styleUrls: ['./edit-task.component.scss']
})
export class EditTaskComponent implements OnInit {

  taskId: number;
  taskForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private taskService: TaskService) {
  }

  ngOnInit() {
    this.loading = true;
    this.taskService.get(this.taskId).subscribe(task => {
      this.taskForm = this.fb.group({
        displayName: [task.displayName, Validators.required],
        description: [task.description, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.taskService.update(this.taskId, this.taskForm.value).subscribe(
      (task) => {
        this.closeModal(task);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(task: Task) {
    this.activeModal.close(task);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
