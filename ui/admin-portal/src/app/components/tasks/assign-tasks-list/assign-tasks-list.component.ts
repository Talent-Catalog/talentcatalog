import {Component, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Task} from "../../../model/candidate";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {SavedList} from "../../../model/saved-list";
import {TaskService} from "../../../services/task.service";

@Component({
  selector: 'app-assign-tasks-list',
  templateUrl: './assign-tasks-list.component.html',
  styleUrls: ['./assign-tasks-list.component.scss']
})
export class AssignTasksListComponent implements OnInit {
  assignForm: FormGroup;
  tasks: Task[];
  allTasks: Task[];
  savedList: SavedList;
  loading;
  error;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private modalService: NgbModal,
              private taskService: TaskService) { }

  ngOnInit(): void {
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

  setTasks(candidateSource: any) {
    this.savedList = candidateSource;
    this.tasks = candidateSource.tasks;
  }

  onSave() {
    this.activeModal.close();
  }

  cancel() {
    this.activeModal.dismiss();
  }

  removeTask(task: Task) {
    const confirmationModal = this.modalService.open(ConfirmationComponent, {scrollable: true});
    confirmationModal.componentInstance.title =
      "Are you sure you want to remove " + task.name + " from the list " + this.savedList.name + "?";
    confirmationModal.componentInstance.message =
      "Note: Removing this task (or task list with sub tasks) will deactivate the task from all candidates within the list. "

    confirmationModal.result
      .then((result) => {
        if (result === true) {
          // todo remove task assignments from all candidates in list
        }
      },
        error => this.error = error
      )
      .catch();
  }

}
