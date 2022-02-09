import {Component, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {SavedList} from "../../../model/saved-list";
import {TaskService} from "../../../services/task.service";
import {AssignTaskToListRequest, TaskAssignmentService} from "../../../services/task-assignment.service";
import {Task} from "../../../model/task";

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
  estDate: Date;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private modalService: NgbModal,
              private taskService: TaskService,
              private taskAssignmentService: TaskAssignmentService) { }

  ngOnInit(): void {
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
    this.loading = true;

    const task: Task = this.assignForm.value.task;

    //Construct request
    const request: AssignTaskToListRequest = {
      savedListId: this.savedList.id,
      taskId: task.id,
      dueDate: this.assignForm.value.dueDate
    }
    this.taskAssignmentService.assignTaskToList(request).subscribe(
      () => {
        this.activeModal.close();
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

  // Allow to search for either a task name or a task type.
  searchTypeOrName = (searchTerm: string, item: any) => {
    return item.taskType.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1 || item.name.toLowerCase().indexOf(searchTerm.toLowerCase()) > -1;
  }

}
