import {Component, OnDestroy, OnInit} from '@angular/core';
import {RegistrationService} from "../../services/registration.service";
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {

  constructor(public registrationService: RegistrationService,
              public authService: AuthService,
              public router: Router) { }

  ngOnInit() {
    this.registrationService.start();
  }

  ngOnDestroy(): void {
    this.registrationService.stop();
  }

  logout() {
    this.authService.logout().subscribe(() => this.router.navigate(['']));
  }
}

