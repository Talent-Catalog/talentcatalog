import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../../model/user";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthService} from "../../../services/auth.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {isAdminUser} from "../../../model/base";
import {TaskService} from "../../../services/task.service";
import {SearchResults} from "../../../model/search-results";
import {Task} from "../../../model/task";
import {EditTaskComponent} from "./edit/edit-task.component";
import {CreateTaskComponent} from "./create/create-task.component";

@Component({
  selector: 'app-search-tasks',
  templateUrl: './search-tasks.component.html',
  styleUrls: ['./search-tasks.component.scss']
})
export class SearchTasksComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Task>;


  constructor(private fb: FormBuilder,
              private taskService: TaskService,
              private modalService: NgbModal,
              private authService: AuthService) {
  }

  ngOnInit() {
    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      status: ['active'],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
  }

  onChanges(): void {
    /* SEARCH ON CHANGE*/
    this.searchForm.valueChanges
    .pipe(
      debounceTime(400),
      distinctUntilChanged()
    )
    .subscribe(res => {
      this.search();
    });
    this.search();
  }

  search() {
    this.loading = true;
    const request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = ['id'];
    request.sortDirection = 'ASC';
    this.taskService.searchPaged(request).subscribe(results => {
        this.results = results;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  isAnAdmin(): boolean {
    return isAdminUser(this.authService);
  }

  addTask() {
    const addTaskModal = this.modalService.open(CreateTaskComponent, {
      centered: true,
      backdrop: 'static'
    });

    addTaskModal.result
      .then((user) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editTask(task: Task) {
    const editTaskModal = this.modalService.open(EditTaskComponent, {
      centered: true,
      backdrop: 'static'
    });

    editTaskModal.componentInstance.taskId = task.id;

    editTaskModal.result
      .then(() => this.search())
      .catch(() => { /* Isn't possible */ });
  }

}
