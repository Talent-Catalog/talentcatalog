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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {CandidateService} from '../../../services/candidate.service';
import {Router} from '@angular/router';
import {AuthorizationService} from "../../../services/authorization.service";

@Component({
  selector: 'app-candidate-name-num-search',
  templateUrl: './candidate-name-num-search.component.html',
  styleUrls: ['./candidate-name-num-search.component.scss']
})
export class CandidateNameNumSearchComponent implements OnInit {

  @Input() handleSelect: string;
  @Input() displayValue: string;
  @Output() candChange = new EventEmitter<string>();

  doNumberOrNameSearch;
  searchFailed: boolean;
  searching: boolean;
  error;
  loggedInUser: User;
  placeholder: string;

  constructor(private authService: AuthorizationService,
    private candidateService: CandidateService,
              private router: Router) { }

  ngOnInit(): void {
    // If no candidate to display in the input field, set to null and display the placeholder text.
    if (this.displayValue === undefined) {
      this.displayValue = null;
    }

    this.doNumberOrNameSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(candidateNumberOrName =>
          this.candidateService.findByCandidateNumberOrName({candidateNumberOrName: candidateNumberOrName, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );
  }

  get placeholderText(): string {
    let text = '';
    if (this.displayValue === null) {
      if (this.isUserLimited()) {
        text = 'Candidate number...';
      } else {
        text = 'Name or number...'
      }
    }
    return text;
  }

  renderCandidateRow(candidate: Candidate) {
    if (this.isUserLimited()) {
      return candidate.candidateNumber;
    } else {
      return candidate.candidateNumber + ": " + candidate.user.firstName + " " + candidate.user.lastName;
    }
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
    // If we only want to display the selected candidate and handle them, we render the row and emit the candidate. Otherwise it will open
    // the candidate into a new tab (like the header)
    if (this.handleSelect === 'displayOnly') {
      input.value = this.renderCandidateRow($event.item);
      this.candChange.emit($event.item)
    } else {
      input.value = ''
      this.router.navigate(['candidate',  $event.item.candidateNumber]);
    }
  }

  isUserLimited(): boolean {
    return !this.authService.canViewCandidateName();
  }

}
