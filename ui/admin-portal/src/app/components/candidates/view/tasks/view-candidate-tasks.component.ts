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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import * as moment from 'moment';
import {Candidate} from "../../../../model/candidate";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  AssignTasksCandidateComponent
} from "../../../tasks/assign-tasks-candidate/assign-tasks-candidate.component";
import {EditTaskAssignmentComponent} from "./edit/edit-task-assignment.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {CreateTaskAssignmentRequest, TaskAssignmentService} from "../../../../services/task-assignment.service";
import {TaskAssignment, taskAssignmentSort} from "../../../../model/task-assignment";
import {
  CandidateAttachmentService,
  ListByUploadTypeRequest
} from "../../../../services/candidate-attachment.service";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {TaskType} from "../../../../model/task";
import {ViewResponseComponent} from "./view-response/view-response.component";
import {Status} from "../../../../model/base";
import {TaskService} from 'src/app/services/task.service';
import {DuolingoCouponService} from 'src/app/services/duolingo-coupon.service';
import {DuolingoCouponResponse} from 'src/app/model/duolingo-coupon';

@Component({
  selector: 'app-view-candidate-tasks',
  templateUrl: './view-candidate-tasks.component.html',
  styleUrls: ['./view-candidate-tasks.component.scss']
})
export class ViewCandidateTasksComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  loading;
  loadingResponse;
  error;
  saving;
  ongoingTasks: TaskAssignment[];
  completedTasks: TaskAssignment[];
  inactiveTasks: TaskAssignment[];
  today: Date;

  constructor(private candidateService: CandidateService,
              private candidateAttachmentService: CandidateAttachmentService,
              private taskAssignmentService: TaskAssignmentService,
              private taskService: TaskService,
              private duolingoCouponService: DuolingoCouponService,
              private modalService: NgbModal) { }

  ngOnInit(): void {
    this.today = new Date();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.filterTasks();
    }
  }

  filterTasks() {
    if (this.candidate.taskAssignments) {
      this.ongoingTasks = this.candidate.taskAssignments.filter(t =>
        t.completedDate == null &&
        t.abandonedDate == null &&
        t.status === Status.active).sort(taskAssignmentSort);
      this.completedTasks = this.candidate.taskAssignments.filter(t =>
        t.completedDate != null ||
        t.abandonedDate != null).sort(taskAssignmentSort);
      this.inactiveTasks = this.candidate.taskAssignments.filter(t =>
        t.status === Status.inactive);
    } else {
      this.ongoingTasks = [];
      this.completedTasks = [];
    }
  }

  isOverdue(ta: TaskAssignment) {
    return (new Date(ta.dueDate) < this.today) && !ta.task.optional;
  }

  assignTask() {
    const assignTaskCandidateModal = this.modalService.open(AssignTasksCandidateComponent, {
      centered: true,
      backdrop: 'static'
    });

    assignTaskCandidateModal.componentInstance.candidateId = this.candidate.id;

    assignTaskCandidateModal.result
      .then((taskAssignment: TaskAssignment) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

  async sendDuolingoCoupon() {

    const duolingoTask = await this.taskService.listTasks().toPromise();
    const duolingoTaskId = duolingoTask.find(task => task.name === 'duolingoTest')?.id;

    if (!duolingoTaskId) {
      this.error = 'Duolingo English Test task not found';
      return;
    }

    const request: CreateTaskAssignmentRequest = {
      candidateId: this.candidate.id,
      taskId: duolingoTaskId,
      dueDate: moment().add(2, 'weeks').toDate()
    }

    const duolingoCoupon : DuolingoCouponResponse  = await this.duolingoCouponService.create(this.candidate.id).toPromise();

    if(duolingoCoupon.status === 'failure') {
      this.error = duolingoCoupon.message;
      return;
    }

    this.taskAssignmentService.createTaskAssignment(request).subscribe(
      (taskAssignment: TaskAssignment) => {
        this.candidateService.updateCandidate();
      },
      error => {
        this.error = error;
      }
    )
  }

  editTaskAssignment(ta: TaskAssignment) {
    const editTaskAssignmentModal = this.modalService.open(EditTaskAssignmentComponent, {
      centered: true,
      backdrop: 'static'
    });

    editTaskAssignmentModal.componentInstance.taskAssignment = ta;

    editTaskAssignmentModal.result
      .then((taskAssignment) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

  deleteTaskAssignment(ta: TaskAssignment) {
    const deleteTaskAssignmentModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteTaskAssignmentModal.componentInstance.message = "Are you sure you want to delete the task '"
      + ta.task.displayName + "' from the tasks assigned to "
      + this.candidate.user.firstName + " " + this.candidate.user.lastName + "?";

    deleteTaskAssignmentModal.result
      .then((result) => {
        if (result === true) {
          this.taskAssignmentService.removeTaskAssignment(ta.id).subscribe(
            () => {
              this.candidateService.updateCandidate();
              this.saving = false;
            },
            error => {
              this.error = error;
              this.saving = false;
            }
          );
        }
      })
      .catch(() => { /* Isn't possible */
      });
  }

  viewResponse(ta: TaskAssignment) {
    // todo in future it might be the answer to a question, display this answer in a modal?
    if (ta.task.taskType === TaskType.Upload) {
      const request: ListByUploadTypeRequest = {
        candidateId: this.candidate.id,
        uploadType: ta.task.uploadType
      }
      this.candidateAttachmentService.listByType(request).subscribe(
        results => {
          if (results.length > 0) {
            this.openFiles(results)
          }
        },
        error => {
          this.error = error;
        });
    } else if (ta.task?.taskType === TaskType.Question) {
      this.viewQuestionResponse(ta);
    }
  }

  openFiles(ats: CandidateAttachment[]) {
    this.loadingResponse = true;
    for (let i = 0; i < ats.length; i++) {
      window.open(ats[i].url);
    }
    this.loadingResponse = false;
  }

  viewQuestionResponse(ta: TaskAssignment) {
    const viewResponseModal = this.modalService.open(ViewResponseComponent, {
      centered: true,
      backdrop: 'static'
    });

    viewResponseModal.componentInstance.taskAssignment = ta;

    viewResponseModal.result
      .then(() => {
      })
      .catch(() => { /* Isn't possible */
      });
  }
}
