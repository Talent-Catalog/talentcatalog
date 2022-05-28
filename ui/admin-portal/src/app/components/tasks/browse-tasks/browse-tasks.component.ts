import {Component, OnInit, SimpleChanges} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {SearchResults} from "../../../model/search-results";
import {
  CandidateSource,
  CandidateSourceType,
  PagedSearchRequest,
  SearchBy, SearchTaskRequest
} from "../../../model/base";
import {User} from "../../../model/user";
import {LocalStorageService} from "angular-2-local-storage";
import {Router} from "@angular/router";
import {AuthService} from "../../../services/auth.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {indexOfAuditable, SearchSavedSearchRequest} from "../../../model/saved-search";
import {SearchSavedListRequest} from "../../../model/saved-list";
import {TaskService} from "../../../services/task.service";
import {Task} from "../../../model/task";

@Component({
  selector: 'app-browse-tasks',
  templateUrl: './browse-tasks.component.html',
  styleUrls: ['./browse-tasks.component.scss']
})
export class BrowseTasksComponent implements OnInit {
  private filterKeySuffix: string = 'Filter';
  private savedStateKeyPrefix: string = 'BrowseKey';

  searchForm: FormGroup;
  public loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Task>;
  selectedTask: Task;
  selectedIndex = 0;
  loggedInUser: User;

  constructor(private fb: FormBuilder,
              private localStorageService: LocalStorageService,
              private router: Router,
              private authService: AuthService,
              private taskService: TaskService) { }

  ngOnInit() {

    this.loggedInUser = this.authService.getLoggedInUser();

    this.pageNumber = 1;
    this.pageSize = 50;

    this.search();
  }


  search() {

    const req: SearchTaskRequest = {
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: ['name'],
      sortDirection: 'ASC',
    };

    this.loading = true;

    this.taskService.searchPaged(req).subscribe(results => {
        this.results = results;
        if (results.content.length > 0) {
          //Selected previously search if any
          const id: number = this.localStorageService.get(this.savedStateKeyPrefix + "Tasks");
          if (id) {
            this.selectedIndex = indexOfAuditable(id, this.results.content);
            if (this.selectedIndex >= 0) {
              this.selectedTask = this.results.content[this.selectedIndex];
            } else {
              //Select the first search if can't find previous (category of search
              // may have changed)
              this.onSelect(this.results.content[0]);
            }
          } else {
            //Select the first search if no previous
            this.onSelect(this.results.content[0]);
          }
        }

        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  /**
   * Called when a particular source (ie list of search) is selected from browse results
   * of the search of sources.
   * @param source Selected candidate source
   */
  onSelect(task: Task) {
    this.selectedTask = task;

    const id: number = task.id;
    this.localStorageService.set(this.savedStateKeyPrefix + "Tasks", id);

    this.selectedIndex = indexOfAuditable(id, this.results.content);
  }

  // private savedStateKey() {
  //   //This key is constructed from the combination of inputs which are associated with each tab
  //   // in home.component.html
  //   //This key is used to store the last state associated with each tab.
  //
  //   //The standard key is "BrowseKey" + the sourceType (SavedSearch or SaveList) +
  //   // the search by (corresponding to the specific displayed tab)
  //   const key = this.savedStateKeyPrefix + "Tasks"
  //   return key;
  // }

  keyDown(event: KeyboardEvent) {
    const oldSelectedIndex = this.selectedIndex;
    switch (event.key) {
      case 'ArrowUp':
        if (this.selectedIndex > 0) {
          this.selectedIndex--;
        }
        break;
      case 'ArrowDown':
        if (this.selectedIndex < this.results.content.length - 1) {
          this.selectedIndex++;
        }
        break;
    }
    if (this.selectedIndex !== oldSelectedIndex) {
      this.onSelect(this.results.content[this.selectedIndex])
    }
  }

}
