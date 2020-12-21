import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {LanguageService} from '../../services/language.service';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})

export class LandingComponent implements OnInit {

  constructor(private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute,
              private languageService: LanguageService) { }

  ngOnInit() {

    //Need to delay changing language otherwise you get ExpressionChangedAfterItHasBeenCheckedError
    const lang = this.route.snapshot.queryParams['lang'];
    setTimeout(
      () => this.languageService.changeLanguage(lang), 1000
    )

    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/home']);
    }
  }

}
