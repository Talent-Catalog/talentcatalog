import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";

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
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/home';
    });

    this.loginForm = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    })
  }

  login() {
    this.error = null;
    if (this.loginForm.invalid) {
      return;
    }
    if (this.loading) { return; }
    this.loading = true;

    this.authService.login(this.loginForm.value)
      .subscribe(() => {
        this.loading = false;
        this.router.navigateByUrl(this.returnUrl);
      }, error => {
        console.log(error);
        this.error = error;
        this.loading = false;
      });
  }

  logout() {
    this.authService.logout();
  }
}

