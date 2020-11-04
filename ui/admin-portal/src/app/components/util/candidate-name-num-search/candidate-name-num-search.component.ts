import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
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
export class CandidateNameNumSearchComponent implements OnInit, OnChanges {

  @Input() handleSelect: string;
  @Input() form: string;
  @Input() candNumber: string;
  @Output() candNumberChange = new EventEmitter<string>();

  doNumberOrNameSearch;
  searchFailed: boolean;
  searching: boolean;
  error;
  loggedInUser: User;

  constructor(private candidateService: CandidateService,
              private router: Router) { }

  ngOnInit(): void {
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

  ngOnChanges(changes: SimpleChanges) {
    this.candidateService.getByNumber(changes.candNumber.currentValue).subscribe(
      result => {
        this.candNumber = this.renderCandidateRow(result)
      }
    )
  }

  getCandidateFromNumber($event, input) {

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
    if (this.handleSelect === 'displayOnly') {
      input.value = this.renderCandidateRow($event.item);
      this.candNumberChange.emit($event.item.candidateNumber)
    } else {
      this.router.navigate(['candidate',  $event.item.candidateNumber]);
    }
  }

  isUserLimited(): boolean {
    const role = this.loggedInUser ? this.loggedInUser.role : null;
    return role === 'semilimited' || role === 'limited';
  }

}
