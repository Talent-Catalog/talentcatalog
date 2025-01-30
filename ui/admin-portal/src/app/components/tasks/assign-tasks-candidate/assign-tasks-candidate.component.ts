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
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {TaskService} from "../../../services/task.service";
import {
  CreateTaskAssignmentRequest,
  TaskAssignmentService
} from "../../../services/task-assignment.service";
import {TaskAssignment} from "../../../model/task-assignment";
import {Task} from "../../../model/task";
import {DuolingoCouponService} from "../../../services/duolingo-coupon.service";

@Component({
  selector: 'app-assign-tasks-candidate',
  templateUrl: './assign-tasks-candidate.component.html',
  styleUrls: ['./assign-tasks-candidate.component.scss']
})
export class AssignTasksCandidateComponent implements OnInit {
  assignForm: UntypedFormGroup;
  candidateId: number;
  allTasks: Task[];
  loading;
  error;
  saving;
  estDate: Date;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private modalService: NgbModal,
              private taskService: TaskService,
              private duolingoCouponService: DuolingoCouponService,
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

    if(task.name === 'duolingoTest') {
      this.duolingoCouponService.assignCouponToCandidate(this.candidateId).subscribe(
        () => {
          this.activeModal.close();
          this.saving = false;
        },
        error => {
          this.error = error;
          this.saving = false;
        }
      );
    } else {
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
  }

  cancel() {
    this.activeModal.dismiss();
  }

  // Allow to search for either a task name or a task type.
  searchTypeOrName = (searchTerm: string, item: any) => {
    return item.taskType.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1 || item.displayName.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1;
  }

}
