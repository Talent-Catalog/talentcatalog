import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';
import {UserService} from '../../../services/user.service';
import {User} from '../../../model/user';
import {AuthService} from '../../../services/auth.service';
import {Candidate, CandidateIntakeData} from '../../../model/candidate';
import {IntakeComponentBase} from '../intake/IntakeComponentBase';
import {CandidateService} from '../../../services/candidate.service';

@Component({
  selector: 'app-user-date-audit',
  templateUrl: './user-date-audit.component.html',
  styleUrls: ['./user-date-audit.component.scss']
})
export class UserDateAuditComponent extends IntakeComponentBase implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;

  form: FormGroup;
  error: string;
  doNameSearch;
  searchFailed: boolean;
  searching: boolean;
  userDisplay: string;
  loggedInUser: User;
  firstChange: boolean;

  constructor(fb: FormBuilder,
              candidateService: CandidateService,
              private userService: UserService,
              private authService: AuthService,) {
    super(fb, candidateService)
  }


  ngOnInit(): void {
    // If date input is undefined (first change) set flag to true to be used when first date selection made.
    // If there is user input, display the user input. If there isn't user input, display logged in user when date selected.
    this.form = this.fb.group({
      intakeMiniCheckedDate: [this.candidateIntakeData?.intakeMiniCheckedDate],
      intakeMiniCheckedById: [this.candidateIntakeData?.intakeMiniCheckedBy.id],
    });

    if (this.checkedDate == null) {
      this.firstChange = true;
      this.loggedInUser = this.authService.getLoggedInUser();
    }
    if (this.candidateIntakeData?.intakeMiniCheckedBy != null) {
      this.userDisplay = this.renderCandidateRow(this.candidateIntakeData?.intakeMiniCheckedBy);
    }

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
    this.form.controls['intakeMiniCheckedById'].patchValue($event.item.id);
  }

  dateSelection ($event) {
    // If it's the first change, set the user value as the logged in user.
    if (this.firstChange) {
      this.form.controls.intakeMiniCheckedDate.markAsDirty()
      // On first date change autofill with the current logged in user and emit current logged in user.
      this.userDisplay = this.renderCandidateRow(this.loggedInUser);
      this.form.controls['intakeMiniCheckedById'].patchValue(this.loggedInUser.id);
      // After first change, set to false.
      this.firstChange = false;
    }
  }

  clearDate () {
    this.form.controls['intakeMiniCheckedDate'].patchValue('');
  }

  get checkedDate(): string {
    return this.form.controls['intakeMiniCheckedDate'].value;
  }

  get checkedBy(): string {
    return this.form.controls['intakeMiniCheckedBy'].value;
  }

}
