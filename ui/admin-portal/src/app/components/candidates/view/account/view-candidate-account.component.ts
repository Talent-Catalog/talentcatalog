import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../model/candidate';
import {User} from '../../../../model/user';
import {CandidateService} from '../../../../services/candidate.service';
import {UserService} from '../../../../services/user.service';
import {ChangePasswordComponent} from './change-password/change-password.component';
import {ChangeUsernameComponent} from './change-username/change-username.component';

@Component({
  selector: 'app-view-candidate-account',
  templateUrl: './view-candidate-account.component.html',
  styleUrls: ['./view-candidate-account.component.scss']
})
export class ViewCandidateAccountComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  user: User;
  loading: boolean;
  error;

  constructor(private candidateService: CandidateService,
              private userService: UserService,
              private modalService: NgbModal) { }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.userService.get(this.candidate.user.id).subscribe(
        user => {
          console.log(user)
          this.user = user;
          console.log(this.user);
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        });
    }
  }

  updatePassword(user: User) {
    const updatePasswordModal = this.modalService.open(ChangePasswordComponent, {
      centered: true,
      backdrop: 'static'
    });

    updatePasswordModal.componentInstance.user = user;

    updatePasswordModal.result
      .then((user) => this.user = user)
      .catch(() => { /* Isn't possible */ });

  }

  updateUsername(user: User) {
    const updateUsernameModal = this.modalService.open(ChangeUsernameComponent, {
      centered: true,
      backdrop: 'static'
    });

    updateUsernameModal.componentInstance.user = user;

    updateUsernameModal.result
      .then((user) => this.user = user)
      .catch(() => { /* Isn't possible */ });

  }

}
