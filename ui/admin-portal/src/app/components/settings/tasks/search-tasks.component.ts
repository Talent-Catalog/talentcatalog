import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../../model/user";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {SearchTaskRequest} from "../../../model/base";
import {TaskService} from "../../../services/task.service";
import {SearchResults} from "../../../model/search-results";
import {Task} from "../../../model/task";
import {EditTaskComponent} from "./edit/edit-task.component";

@Component({
  selector: 'app-search-tasks',
  templateUrl: './search-tasks.component.html',
  styleUrls: ['./search-tasks.component.scss']
})
export class SearchTasksComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Task>;


  constructor(private fb: UntypedFormBuilder,
              private taskService: TaskService,
              private modalService: NgbModal,
              private authService: AuthorizationService) {
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
    const request: SearchTaskRequest =  {
      keyword: this.searchForm.value.keyword,
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: ['id'],
      sortDirection: 'ASC',
    };
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
    return this.authService.isAnAdmin();
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
