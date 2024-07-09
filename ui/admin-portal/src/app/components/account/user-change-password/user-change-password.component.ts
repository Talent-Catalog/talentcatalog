import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {environment} from "../../../../environments/environment";

@Component({
  selector: 'app-user-change-password',
  templateUrl: './user-change-password.component.html',
  styleUrls: ['./user-change-password.component.scss']
})
export class UserChangePasswordComponent implements OnInit {

  loading: boolean;
  reset: boolean;
  error: any;
  tokenInvalid: boolean;
  changePasswordForm: FormGroup;
  updated: boolean;
  token: string;

  backgroundImage: string;

  constructor(private fb: FormBuilder,
              private userService: UserService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.backgroundImage = `url(${environment.assetBaseUrl}/assets/images/login-splash-v2.2.1.png)`;

    this.loading = false;
    this.reset = false;
    this.error = null;
    this.updated = false;
    this.tokenInvalid = false;

    this.route.paramMap
    .subscribe(params => {
      this.token = params.get('token');
      if (this.token) {
        const request = {
          token: this.token
        };

        this.userService.checkPasswordResetToken(request).subscribe(() => {
              this.loading = false;
              this.reset = true;
              this.changePasswordForm = this.fb.group({
                token: [this.token, Validators.required],
                password: ['', Validators.required],
                passwordConfirmation: ['', Validators.required]
              });
            },
            error => {
              this.error = error;
              this.tokenInvalid = true;
            });
      } else {
        this.changePasswordForm = this.fb.group({
          oldPassword: ['', Validators.required],
          password: ['', Validators.required],
          passwordConfirmation: ['', Validators.required]
        });
      }
    });
  }

  updatePassword() {
    this.updated = false;
    this.error = null;
    if (this.reset) {
      this.userService.resetPassword(this.changePasswordForm.value).subscribe(
          () => {
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 2000);
            this.resetForm();
            this.updated = true;
          },
          (error) => {
            this.error = error;
          }
      );
    }
  }

  resetForm() {
    if (this.reset) {
      this.changePasswordForm.patchValue({
        token: '',
        password: '',
        passwordConfirmation: '',
      });    } else {
      this.changePasswordForm.patchValue({
        oldPassword: '',
        password: '',
        passwordConfirmation: '',
      });
    }
    const keys: string[] = Object.keys(this.changePasswordForm.controls);
    for (const key of keys) {
      this.changePasswordForm.controls[key].markAsPristine();
    }
  }
}
