import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from '../../../../../services/user.service';
import {User} from '../../../../../model/user';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  user: User;

  loading: boolean;
  reset: boolean;
  error: any;
  tokenInvalid: boolean;
  form: FormGroup;
  updated: boolean;

  constructor(private fb: FormBuilder,
              private activeModal: NgbActiveModal,
              private userService: UserService) {
  }

  ngOnInit() {
    this.loading = false;
    this.reset = false;
    this.error = null;
    this.updated = false;
    this.tokenInvalid = false;
    this.form = this.fb.group({
        password: ['', Validators.required],
        passwordConfirmation: ['', Validators.required]
    });
  }

  updatePassword() {
    this.updated = false;
    this.error = null;
    this.userService.updatePassword(this.user.id, this.form.value).subscribe(
      () => {
        this.closeModal(this.user);
        this.updated = true;
      },
      (error) => {
        this.error = error;
      }
    );
  }

  closeModal(user: User) {
    this.activeModal.close(user);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
