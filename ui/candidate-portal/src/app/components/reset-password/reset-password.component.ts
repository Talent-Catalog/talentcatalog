import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {UserService} from '../../services/user.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

  loading: boolean;
  error: any;
  form: FormGroup;
  updated: boolean;

  constructor(private fb: FormBuilder,
              private userService: UserService) {
  }

  ngOnInit() {
    this.loading = false;
    this.error = null;
    this.updated = false;
    this.form = this.fb.group({
      email: ['', Validators.required]
    });
  }

  sendResetEmail() {
    this.updated = false;
    this.error = null;
    this.userService.sendResetPassword(this.form.value).subscribe(
      () => {
        this.resetForm();
        this.updated = true;
      },
      (error) => {
        this.error = error;
      }
    );
  }

  resetForm() {
    this.form.patchValue({
      email: '',
    });
    const keys: string[] = Object.keys(this.form.controls);
    for (const key of keys) {
      this.form.controls[key].markAsPristine();
    }
  }

}
