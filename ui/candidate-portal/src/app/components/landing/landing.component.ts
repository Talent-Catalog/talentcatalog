import {Component, HostBinding, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {LanguageService} from '../../services/language.service';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})

export class LandingComponent implements OnInit {

  @HostBinding('class.rtl-wrapper') rtl: boolean;
  lang: string;

  constructor(private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute,
              private languageService: LanguageService) { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {this.lang = params.get('lang')});
    // Add .rtl-wrapper class to app root if the language is arabic
    this.rtl = this.lang === 'ar';
    this.languageService.setLanguage(this.lang);

    // If there is a loggedInUser, go directly to profile.
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/home']);
    }
  }

}
