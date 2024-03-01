import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user.service";
import {SendResetPasswordEmailRequest} from "../../../model/candidate";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

  loading: boolean;
  error: any;
  resetPasswordForm: FormGroup;
  updated: boolean;

  constructor(private fb: FormBuilder,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.loading = false;
    this.error = null;
    this.updated = false;
    this.resetPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  get email(): string {
    return this.resetPasswordForm.value.email;
  }

  resetForm() {
    this.resetPasswordForm.patchValue({
      email: '',
    });
    const keys: string[] = Object.keys(this.resetPasswordForm.controls);
    for (const key of keys) {
      this.resetPasswordForm.controls[key].markAsPristine();
    }
  }

  sendResetEmail() {
    this.updated = false;
    this.error = null;

    const req: SendResetPasswordEmailRequest = new SendResetPasswordEmailRequest();
    req.email = this.email;
    req.isAdmin = true;

    this.userService.sendResetPassword(req).subscribe(
        () => {
          this.resetForm();
          this.updated = true;
        },
        (error) => {
          this.error = error;
        }
    );


  }

}
