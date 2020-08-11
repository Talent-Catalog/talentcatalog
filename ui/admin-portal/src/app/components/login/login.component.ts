import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";
import {LoginRequest} from "../../model/base";
import {ReCaptchaV3Service} from "ng-recaptcha";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  loading: boolean;
  returnUrl: string;
  error;

  constructor(private builder: FormBuilder,
              private authService: AuthService,
              private reCaptchaV3Service: ReCaptchaV3Service,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '';
    });

    this.loginForm = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    })
  }

  get username(): string {
    return this.loginForm.value.username;
  }

  get password(): string {
    return this.loginForm.value.password;
  }

  login() {
    this.error = null;
    if (this.loginForm.invalid) {
      return;
    }
    if (this.loading) { return; }
    this.loading = true;

    const action = 'login';
    this.reCaptchaV3Service.execute(action).subscribe(
      (token) => this.loginWithToken(token),
      (error) => {
        console.log(error);
      }
    );
  }

  logout() {
    this.authService.logout();
  }

  private loginWithToken(token: string) {
    const req: LoginRequest = new LoginRequest();
    req.username = this.username;
    req.password = this.password;
    req.reCaptchaV3Token = token;

    this.authService.login(req)
      .subscribe(() => {
        this.loading = false;
        this.router.navigateByUrl(this.returnUrl);
      }, error => {
        // console.log(error);
        this.error = error;
        this.loading = false;
      });

  }
}

