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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {SavedList} from "../../../model/saved-list";
import {TaskService} from "../../../services/task.service";
import {TaskAssignmentService, TaskListRequest} from "../../../services/task-assignment.service";
import {Task} from "../../../model/task";
import {SavedListService} from "../../../services/saved-list.service";
import {DuolingoCouponService} from "../../../services/duolingo-coupon.service";

@Component({
  selector: 'app-assign-tasks-list',
  templateUrl: './assign-tasks-list.component.html',
  styleUrls: ['./assign-tasks-list.component.scss']
})
export class AssignTasksListComponent implements OnInit {
  assignForm: UntypedFormGroup;
  filteredTaskAssociations: Task[];
  allTasks: Task[];
  savedList: SavedList;
  loading;
  error;
  estDate: Date;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private modalService: NgbModal,
              private savedListService: SavedListService,
              private taskService: TaskService,
              private duolingoCouponService: DuolingoCouponService,
              private taskAssignmentService: TaskAssignmentService) { }

  ngOnInit(): void {
    this.assignForm = this.fb.group({
      task: [null, [Validators.required]],
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
      },

      error => {
        this.error = error;
      }

    );
    this.loading = false;
  }

  setTasks(savedList: SavedList) {
    this.savedList = savedList;
    //todo This should really do a search with the current filter. Can we make the search
    //part of the component so we can access the filter data in this code.
    this.filteredTaskAssociations = savedList.tasks;
  }

  refreshTaskAssociations() {
    this.savedListService.get(this.savedList.id).subscribe(
      (result) => {
        this.savedList.tasks = result.tasks;

        //todo This should really do a search with the current filter. Can we make the search
        //part of the component so we can access the filter data in this code.
        this.filteredTaskAssociations = result.tasks;
      }, (error) => {
        this.error = error;
      }
    )
  }

  onSave() {
    this.loading = true;
    this.error = null;

    const task: Task = this.assignForm.value.task;

    //Construct request
    const request: TaskListRequest = {
      savedListId: this.savedList.id,
      taskId: task.id,
    }
    if(task.name === 'claimCouponButton') {
      this.duolingoCouponService.assignCouponToList(this.savedList.id).subscribe(
        () => {
          this.refreshTaskAssociations();
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        }
      )
    } else {
      this.taskAssignmentService.assignTaskToList(request).subscribe(
        () => {
          this.refreshTaskAssociations();
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        }
      );
    }
  }

  close() {
    this.activeModal.close();
  }

  removeTask(task: Task) {
    const confirmationModal = this.modalService.open(ConfirmationComponent, {scrollable: true});
    confirmationModal.componentInstance.title =
      "Are you sure you want to remove " + task.displayName + " from the associated list " + this.savedList.name + "?";
    confirmationModal.componentInstance.message =
      "Note: Removing this task association will make the task inactive for any candidates within the list who have not completed the task. "

    confirmationModal.result
      .then((result) => {
        if (result === true) {
          this.doRemoveTask(task);
        }
      })
      .catch();
  }

  private doRemoveTask(task: Task) {
    this.loading = true;
    this.error = null;

    //Construct request
    const request: TaskListRequest = {
      savedListId: this.savedList.id,
      taskId: task.id,
    }
    this.taskAssignmentService.removeTaskFromList(request).subscribe(
      () => {
        this.refreshTaskAssociations();
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  search(target): void {
    if (target.value != null) {
      this.filteredTaskAssociations = this.savedList.tasks.filter((val) => val.displayName.toLowerCase().includes(target.value));
    } else {
      this.filteredTaskAssociations = this.savedList.tasks;
    }
  }

  // Allow to search for either a task display name or a task type.
  searchTypeOrName = (searchTerm: string, item: any) => {
    return item.taskType.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1 || item.displayName.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1;
  }

  monitorTask(t: Task) {
    this.activeModal.close(t);
  }
}
