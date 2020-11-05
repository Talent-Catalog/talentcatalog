import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {CandidateService} from '../../../services/candidate.service';
import {Router} from '@angular/router';

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

  constructor(private candidateService: CandidateService,
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
    const role = this.loggedInUser ? this.loggedInUser.role : null;
    return role === 'semilimited' || role === 'limited';
  }

}
