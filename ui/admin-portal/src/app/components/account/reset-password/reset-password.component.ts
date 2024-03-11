import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user.service";
import {SendResetPasswordEmailRequest} from "../../../model/candidate";
import {environment} from "../../../../environments/environment";

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

  backgroundImage: string;

  constructor(private fb: FormBuilder,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.backgroundImage = `url(${environment.assetBaseUrl}/assets/images/login-splash-v2.2.1.png)`;
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
