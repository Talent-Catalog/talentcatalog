import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {UserService} from '../../services/user.service';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  loading: boolean;
  reset: boolean;
  error: any;
  tokenInvalid: boolean;
  form: FormGroup;
  updated: boolean;
  token: string;

  constructor(private fb: FormBuilder,
              private userService: UserService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
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
              this.form = this.fb.group({
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
           this.form = this.fb.group({
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
        this.userService.resetPassword(this.form.value).subscribe(
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
    } else {
        this.userService.updatePassword(this.form.value).subscribe(
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

  resetForm() {
    if (this.reset) {
        this.form.patchValue({
          token: '',
          password: '',
          passwordConfirmation: '',
        });    } else {
        this.form.patchValue({
          oldPassword: '',
          password: '',
          passwordConfirmation: '',
        });
    }
    const keys: string[] = Object.keys(this.form.controls);
    for (const key of keys) {
      this.form.controls[key].markAsPristine();
    }
  }

}
