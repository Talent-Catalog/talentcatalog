import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';
import {UserService} from '../../../services/user.service';
import {User} from '../../../model/user';

@Component({
  selector: 'app-user-date-audit',
  templateUrl: './user-date-audit.component.html',
  styleUrls: ['./user-date-audit.component.scss']
})
export class UserDateAuditComponent implements OnInit {

  @Input() handleSelect: string;
  @Input() displayValue: string;
  @Output() userChange = new EventEmitter<string>();

  form: FormGroup;
  error: string;
  doNameSearch;
  searchFailed: boolean;
  searching: boolean;

  constructor(private fb: FormBuilder,
              private userService: UserService) { }

  ngOnInit(): void {
    this.doNameSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(usersName =>
          this.userService.findByUsersName({usersName: usersName, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );

    this.form = this.fb.group({
      completedBy: [null],
      completedDate: [null],
    });
  }

  renderCandidateRow(user: User) {
      return user.id + ": " + user.firstName + " " + user.lastName;
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
    // If we only want to display the selected candidate and handle them, we render the row and emit the candidate. Otherwise it will open
    // the candidate into a new tab (like the header)
      input.value = this.renderCandidateRow($event.item);
      this.userChange.emit($event.item)
  }

}
