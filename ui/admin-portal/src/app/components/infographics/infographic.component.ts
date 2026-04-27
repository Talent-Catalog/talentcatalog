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
import {CandidateStatService, CandidateStatsRequest} from "../../services/candidate-stat.service";
import {StatReport} from "../../model/stat-report";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {SavedList, SearchSavedListRequest} from "../../model/saved-list";
import {SavedListService} from "../../services/saved-list.service";
import {ActivatedRoute} from "@angular/router";
import {SavedSearch} from "../../model/saved-search";
import {forkJoin} from "rxjs";
import {SavedSearchService} from "../../services/saved-search.service";
import {saveBlob} from "../../util/file";
import {EnumOption, enumOptions} from "../../util/enum";
import {Stat} from "../../model/stat";

@Component({
  selector: 'app-infographic',
  templateUrl: './infographic.component.html',
  styleUrls: ['./infographic.component.scss']
})
export class InfographicComponent implements OnInit {

  public statOptions: EnumOption[] = enumOptions(Stat);

  loading: boolean = false;
  dataLoaded: boolean = false;
  error: any;
  lists: SavedList[] = [];
  searches: SavedSearch[] = [];
  statReports: StatReport[];
  statsFilter: UntypedFormGroup;
  statsName: string;
  listFromUrl: boolean = false;

  constructor(private route: ActivatedRoute,
              private statService: CandidateStatService,
              private savedListService: SavedListService,
              private savedSearchService: SavedSearchService,
              private fb: UntypedFormBuilder) {
  }

  ngOnInit() {
    this.dataLoaded = false;

    this.statsFilter = this.fb.group({
      savedList: [null],
      savedSearch: [null],
      dateFrom: ['', [Validators.required]],
      dateTo: ['', [Validators.required]],
      selectedStats: [[]]
    });

    this.loadListsAndSearches();
  }

  get dateFrom(): string { return this.statsFilter.value.dateFrom; }
  get dateTo(): string { return this.statsFilter.value.dateTo; }
  get savedList(): SavedList {
    const savedList: SavedList = this.statsFilter.value.savedList;
    //Control always returns an object
    return savedList;
  }
  get savedSearch(): SavedSearch {
    const savedSearch: SavedSearch = this.statsFilter.value.savedSearch;
    //Control always returns an object
    return savedSearch;
  }
  get selectedStats(): Stat[] {
    return this.statsFilter.value.selectedStats;
  }

  private loadListsAndSearches() {
    /*load all our visible lists and searches */
    this.loading = true;
    this.error = null;

    const request: SearchSavedListRequest = {
      owned: true,
      shared: true,
      global: true
    };


    forkJoin( {
      'lists': this.savedListService.search(request),
      'searches': this.savedSearchService.search(request)
    }).subscribe(
      results => {
        this.loading = false;
        this.lists = results['lists'];
        this.searches = results['searches'];

        this.initializeFilterFields();
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  /**
   * Initializes the saved list and saved search filters based on any url parameters that were
   * supplied
   */
  private initializeFilterFields() {
    this.route.paramMap.subscribe(params => {
      const id: number = +params.get('id');
      const isSavedSearchId = params.get('source') === 'search';
      if (id) {
        this.listFromUrl = true;
        if (isSavedSearchId) {
          this.savedSearchService.get(id).subscribe(
            (savedSearch) => {
              this.statsFilter.controls['savedSearch'].patchValue(savedSearch);
              this.submitStatsRequest();
            }, error => {
              this.error = error;
            });

        } else {
          this.savedListService.get(id).subscribe(
            (savedList) => {
              this.statsFilter.controls['savedList'].patchValue(savedList);
              this.submitStatsRequest();
            }, error => {
              this.error = error;
          });
        }
      }
    });
  }

  submitStatsRequest(){
    this.loading = true;
    this.error = null;

    const request: CandidateStatsRequest = {
      listId: this.savedList == null ? null : this.savedList.id,
      searchId: this.savedSearch == null ? null : this.savedSearch.id,
      dateFrom: this.dateFrom,
      dateTo: this.dateTo,
      selectedStats: this.selectedStats
    }

    if (this.savedList) {
      this.statsName = 'list ' + this.savedList?.name;
    } else if (this.savedSearch) {
      this.statsName = 'search ' + this.savedSearch?.name;
    } else {
      this.statsName = 'all data'
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

      // Add date filter to export csv
      csv.push('"' + 'Exported Date' + '","' + new Date().toUTCString() + '"\n');
      csv.push('"' + 'Date From' + '","' + this.statsFilter.value.dateFrom + '"\n')
      csv.push('"' + 'Date To' + '","' + this.statsFilter.value.dateTo + '"\n')
      if (this.savedList) {
        csv.push('"' + 'List' + '","' + this.savedList.name + '(' + this.savedList.id + ')"\n')
      }
      if (this.savedSearch) {
        csv.push('"' + 'Search' + '","' + this.savedSearch.name + '(' + this.savedSearch.id + ')"\n')
      }
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
      saveBlob(blob, filename);
  }

  scroll(id){
    const elmnt = document.getElementById(id);
    elmnt.scrollIntoView({behavior: "smooth", block: "center"});
  }
}
