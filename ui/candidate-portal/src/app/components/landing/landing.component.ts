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

    //Note that we deliberately use a snapshot rather than a subscribe
    //so that we pick up the lang query even if there is a redirect.
    //eg like login redirects to /home if already authenticated.
    //With a subscribe, the query can be lost in the redirect.
    const lang = this.route.snapshot.queryParams['lang'];
    //Need to delay changing language otherwise you get ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(
      () => this.languageService.changeLanguage(lang), 1000
    )

    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/home']);
    }
  }

}
