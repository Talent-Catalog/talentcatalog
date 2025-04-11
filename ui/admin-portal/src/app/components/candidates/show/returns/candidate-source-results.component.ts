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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {getCandidateSourceNavigation} from '../../../../model/saved-search';
import {Router} from '@angular/router';
import {
  CandidateSourceResultsCacheService
} from '../../../../services/candidate-source-results-cache.service';
import {CandidateSource, DtoType} from '../../../../model/base';
import {CandidateSourceCandidateService} from '../../../../services/candidate-source-candidate.service';
import {AuthorizationService} from '../../../../services/authorization.service';
import {CandidateFieldService} from "../../../../services/candidate-field.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateSourceBaseComponent} from "../candidate-source-base";

@Component({
  selector: 'app-candidate-source-results',
  templateUrl: './candidate-source-results.component.html',
  styleUrls: ['./candidate-source-results.component.scss']
})
export class CandidateSourceResultsComponent extends CandidateSourceBaseComponent implements OnInit, OnChanges {
  @Input() showSourceDetails = true;
  @Output() toggleStarred = new EventEmitter<CandidateSource>();
  @Output() toggleWatch = new EventEmitter<CandidateSource>();
  @Output() copySource = new EventEmitter<CandidateSource>();
  @Output() deleteSource = new EventEmitter<CandidateSource>();
  @Output() editSource = new EventEmitter<CandidateSource>();

  constructor(
      private router: Router,
      protected authorizationService: AuthorizationService,
      protected candidateSourceResultsCacheService: CandidateSourceResultsCacheService,
      protected candidateSourceCandidateService: CandidateSourceCandidateService,
      protected candidateFieldService: CandidateFieldService,
      protected modalService: NgbModal
    ) {
      super(
        authorizationService,
        candidateSourceResultsCacheService,
        candidateSourceCandidateService,
        candidateFieldService,
        modalService);
  };

  ngOnInit() {
    this.longFormat = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Do search if candidate source has changed.
    const change = changes.candidateSource;
    if (change && change.previousValue !== change.currentValue) {
      this.loadSelectedFields();
      this.search(false);
    }
  }

  onOpenSource() {
    //Open source at same page number
    let extras;
    if (this.pageNumber === 1) {
      extras = {};
    } else {
      extras = {queryParams: {pageNumber: this.pageNumber}};
    }
    const urlCommands = getCandidateSourceNavigation(this.candidateSource);
    this.router.navigate(urlCommands, extras);
  }

  search(refresh: boolean) {
    let done = this.checkCache(refresh, false);
    if (!done) {
      // Keep page size smaller for this component (12). It is a preview of the source, so doesn't
      // need to be long.
      // It works better on all screen sizes, especially when we have a long split page.
      this.performSearch(12, DtoType.PREVIEW).subscribe();
    }
  }

  toggleSort(column: string) {
    super.toggleSort(column);
    this.search(true);
  }

  //Pass toggle starred up to BrowseCandidateSourcesComponent for it to
  //do the update and refresh its copy of the candidate source details
  // (which is passed through to all contained components)
  onToggleStarred(source: CandidateSource) {
    this.toggleStarred.emit(source);
  }

  //Pass toggle watch up to BrowseCandidateSourcesComponent for it to
  //do the update and refresh its copy of the candidate source details
  // (which is passed through to all contained components)
  onToggleWatch(source: CandidateSource) {
    this.toggleWatch.emit(source);
  }

  onDeleteSource(source: CandidateSource) {
    this.deleteSource.emit(source);
  }

  onEditSource(source: CandidateSource) {
    this.editSource.emit(source);
  }

  onCopySource(source: CandidateSource) {
    this.copySource.emit(source);
  }

  onPageChange() {
    this.search(true);
  }

  onRefreshRequest() {
    this.search(true);
  }

}
