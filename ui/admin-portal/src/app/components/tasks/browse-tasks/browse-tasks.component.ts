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
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {SearchResults} from "../../../model/search-results";
import {SearchTaskRequest} from "../../../model/base";
import {User} from "../../../model/user";
import {Router} from "@angular/router";
import {indexOfHasId} from "../../../model/saved-search";
import {TaskService} from "../../../services/task.service";
import {Task} from "../../../model/task";
import {AuthenticationService} from "../../../services/authentication.service";
import {LocalStorageService} from "../../../services/local-storage.service";

@Component({
  selector: 'app-browse-tasks',
  templateUrl: './browse-tasks.component.html',
  styleUrls: ['./browse-tasks.component.scss']
})
export class BrowseTasksComponent implements OnInit {
  private filterKeySuffix: string = 'Filter';
  private savedStateKeyPrefix: string = 'BrowseKey';

  searchForm: UntypedFormGroup;
  public loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Task>;
  selectedTask: Task;
  selectedIndex = 0;
  loggedInUser: User;

  constructor(private fb: UntypedFormBuilder,
              private localStorageService: LocalStorageService,
              private router: Router,
              private authenticationService: AuthenticationService,
              private taskService: TaskService) { }

  ngOnInit() {

    this.loggedInUser = this.authenticationService.getLoggedInUser();

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
            this.selectedIndex = indexOfHasId(id, this.results.content);
            if (this.selectedIndex >= 0) {
              this.selectedTask = this.results.content[this.selectedIndex];
            } else {
              //Select the first search if can't find previous (category of search
              // may have changed)
              this.select(this.results.content[0]);
            }
          } else {
            //Select the first search if no previous
            this.select(this.results.content[0]);
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
   * Called when a particular task is selected from browse results
   * @param task Selected task
   */
  select(task: Task) {
    this.selectedTask = task;

    const id: number = task.id;
    this.localStorageService.set(this.savedStateKeyPrefix + "Tasks", id);

    this.selectedIndex = indexOfHasId(id, this.results.content);
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
      this.select(this.results.content[this.selectedIndex])
    }
  }

}
