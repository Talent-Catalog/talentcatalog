import { Component } from '@angular/core';
import {NavigationStart, Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  private showHeader: boolean  = true;

  constructor(private router: Router) {

    router.events.forEach((event) => {
      if (event instanceof NavigationStart) {
        if (event['url'] == '/login' || event['url'].indexOf('/reset-password') != -1) {
          this.showHeader = false;
        } else {
          this.showHeader = true;
        }
      }
    });
  }
}
