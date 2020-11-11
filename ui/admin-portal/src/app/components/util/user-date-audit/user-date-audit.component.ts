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

  @Input() dateInput: string;
  @Input() userInput: User;
  @Output() dateChange = new EventEmitter<string>();
  @Output() userChange = new EventEmitter<User>();

  form: FormGroup;
  error: string;
  doNameSearch;
  searchFailed: boolean;
  searching: boolean;
  user: string;

  constructor(private fb: FormBuilder,
              private userService: UserService) { }

  ngOnInit(): void {
    // Display the input user if it's passed down
    if (this.userInput === undefined) {
      this.user = null;
    } else {
      this.user = this.userInput.id + ': ' + this.userInput.firstName + ' ' + this.userInput.lastName;
    }

    if (this.dateInput === undefined) {this.dateInput = null;}

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
      return user.id + ": " + user.firstName + " " + user.lastName;
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
      input.value = this.renderCandidateRow($event.item);
      this.userChange.emit($event.item)
  }

  dateSelection () {
    this.dateChange.emit(this.dateInput);
  }

  // todo can't clear fields using null. Do we send a 'Removed' string instead?
  clearDate () {
    this.dateInput = null;
    this.dateChange.emit(null);
  }

}
