/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {
  CandidateStatService,
  CandidateStatsRequest
} from "../../services/candidate-stat.service";
import {StatReport} from "../../model/stat-report";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SavedList, SearchSavedListRequest} from "../../model/saved-list";
import {SavedListService} from "../../services/saved-list.service";
import {IDropdownSettings} from "ng-multiselect-dropdown";

@Component({
  selector: 'app-infographic',
  templateUrl: './infographic.component.html',
  styleUrls: ['./infographic.component.scss']
})
export class InfographicComponent implements OnInit {

  loading: boolean = false;
  dataLoaded: boolean = false;
  error: any;
  lists: SavedList[] = [];
  statReports: StatReport[];
  statsFilter: FormGroup;

  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    enableCheckAll: false,
    singleSelection: true,
    allowSearchFilter: true
  };

  constructor(private statService: CandidateStatService,
              private savedListService: SavedListService,
              private fb: FormBuilder) {
  }

  ngOnInit() {
    this.dataLoaded = false;

    this.statsFilter = this.fb.group({
      savedList: [null],
      dateFrom: ['', [Validators.required]],
      dateTo: ['', [Validators.required]]
    });

    this.loadLists();
  }

  get dateFrom(): string { return this.statsFilter.value.dateFrom; }
  get dateTo(): string { return this.statsFilter.value.dateTo; }
  get savedListId(): number {
    const savedList: SavedList = this.statsFilter.value.savedList;
    //Control always returns an array
    return savedList == null ? 0 : savedList[0].id;
  }

  private loadLists() {
    /*load all our non fixed lists */
    this.loading = true;
    const request: SearchSavedListRequest = {
      owned: true,
      shared: true,
      fixed: false
    };

    this.savedListService.search(request).subscribe(
      (results) => {
        this.lists = results;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  submitStatsRequest(){
    this.loading = true;

    const request: CandidateStatsRequest = {
      listId: this.savedListId,
      dateFrom: this.dateFrom,
      dateTo: this.dateTo
    }
    this.statService.getAllStats(request).subscribe(result => {
        this.loading = false;
        this.statReports = result;
        this.dataLoaded = true;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    )

  }

  exportStats() {
      const options = {type: 'text/csv;charset=utf-8;'};
      const filename = 'stats.csv';

      const csv: string[] = [];

      //todo Need to add list/save search names in here

      // Add date filter to export csv
      csv.push('"' + 'Exported Date' + '","' + new Date().toUTCString() + '"\n');
      csv.push('"' + 'Date From' + '","' + this.statsFilter.value.dateFrom + '"\n')
      csv.push('"' + 'Date To' + '","' + this.statsFilter.value.dateTo + '"\n')
      csv.push('\n');

      // Add data to export csv
      for (const statReport of this.statReports) {
        csv.push(statReport.name + '\n');
        for (const row of statReport.rows) {
          csv.push('"' + row.label + '","' + row.value.toString() + '"\n')
        }
        csv.push('\n');
      }

    const blob = new Blob(csv, options);

    if (navigator.msSaveBlob) {
      // IE 10+
      navigator.msSaveBlob(blob, filename);
    } else {
      const link = document.createElement('a');
      // Browsers that support HTML5 download attribute
      if (link.download !== undefined) {
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    }
  }

}
