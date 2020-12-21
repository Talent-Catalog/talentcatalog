import {Component, OnDestroy, OnInit} from '@angular/core';
import {RegistrationService} from "../../services/registration.service";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";
import {LanguageService} from "../../services/language.service";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {

  constructor(public registrationService: RegistrationService,
              public authService: AuthService,
              private route: ActivatedRoute,
              private languageService: LanguageService,
              public router: Router) { }

  ngOnInit() {

    //Need to delay changing language otherwise you get ExpressionChangedAfterItHasBeenCheckedError
    const lang = this.route.snapshot.queryParams['lang'];
    setTimeout(
      () => this.languageService.changeLanguage(lang), 1000
    )

    this.registrationService.start();
  }

  ngOnDestroy(): void {
    this.registrationService.stop();
  }

  logout() {
    this.authService.logout().subscribe(() => this.router.navigate(['']));
  }
}

