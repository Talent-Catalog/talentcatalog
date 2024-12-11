/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {TaskService, UpdateTaskRequest} from "../../../../services/task.service";
import {Task} from "../../../../model/task";

@Component({
  selector: 'app-edit-task',
  templateUrl: './edit-task.component.html',
  styleUrls: ['./edit-task.component.scss']
})
export class EditTaskComponent implements OnInit {

  taskId: number;
  taskForm: UntypedFormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private taskService: TaskService) {
  }

  ngOnInit() {
    this.loading = true;
    this.taskService.get(this.taskId).subscribe(task => {
      this.taskForm = this.fb.group({
        displayName: [task.displayName, Validators.required],
        description: [task.description, Validators.required],
        daysToComplete: [task.daysToComplete, Validators.required],
        optional: [task.optional, Validators.required],
        helpLink: [task.helpLink],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    const request: UpdateTaskRequest = {
      displayName: this.taskForm.value.displayName,
      description: this.taskForm.value.description,
      daysToComplete: this.taskForm.value.daysToComplete,
      optional: this.taskForm.value.optional,
      helpLink: this.taskForm.value.helpLink,
    }
    this.taskService.update(this.taskId, request).subscribe(
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
