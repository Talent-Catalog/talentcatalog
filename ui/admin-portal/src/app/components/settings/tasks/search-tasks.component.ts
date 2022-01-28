import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../../model/user";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SystemLanguage} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthService} from "../../../services/auth.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {CreateLanguageComponent} from "../languages/create/create-language.component";
import {isAdminUser, PagedSearchRequest} from "../../../model/base";
import {indexOfAuditable} from "../../../model/saved-search";
import {TaskService} from "../../../services/task.service";
import {Task} from "../../../model/candidate";
import {SearchResults} from "../../../model/search-results";

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
    const req: PagedSearchRequest = {
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: ['name'],
      sortDirection: 'ASC',
    };
    this.taskService.searchPaged(req).subscribe(results => {
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

}
