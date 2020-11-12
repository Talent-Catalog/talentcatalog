import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';
import {UserService} from '../../../services/user.service';
import {User} from '../../../model/user';
import {AuthService} from '../../../services/auth.service';

@Component({
  selector: 'app-user-date-audit',
  templateUrl: './user-date-audit.component.html',
  styleUrls: ['./user-date-audit.component.scss']
})
export class UserDateAuditComponent implements OnInit {

  @Input() dateInput: string;
  @Input() userInput: User;
  @Output() dateChange = new EventEmitter<string>();
  @Output() userChange = new EventEmitter<User>();

  form: FormGroup;
  error: string;
  doNameSearch;
  searchFailed: boolean;
  searching: boolean;
  userDisplay: string;
  loggedInUser: User;
  firstChange: boolean;

  constructor(private fb: FormBuilder,
              private userService: UserService,
              private authService: AuthService) { }

  ngOnInit(): void {
    // If date input is undefined (first change) set flag to true to be used when first date selection made.
    if (this.dateInput == null) {
      this.firstChange = true;
    }
    // If there is user input, display the user input. If there isn't user input, display logged in user when date selected.
    if (this.userInput) {
      this.userDisplay = this.renderCandidateRow(this.userInput);
    }

    this.loggedInUser = this.authService.getLoggedInUser();

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
  }

  renderCandidateRow(user: User) {
      return user?.id + ": " + user?.firstName + " " + user?.lastName;
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
      input.value = this.renderCandidateRow($event.item);
      this.userChange.emit($event.item)
  }

  dateSelection () {
    if (this.firstChange) {
      this.dateChange.emit(this.dateInput);
      // On first date change autofill with the current logged in user and emit current logged in user.
      this.userDisplay = this.renderCandidateRow(this.loggedInUser);
      this.userChange.emit(this.loggedInUser);
      // After first change, set to false.
      this.firstChange = false;
    } else {
      this.dateChange.emit(this.dateInput)
    }
  }

  clearDate () {
    this.dateInput = null;
    this.dateChange.emit('');
  }

}
