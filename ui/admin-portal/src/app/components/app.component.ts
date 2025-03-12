/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Title} from "@angular/platform-browser";
import {filter, map} from "rxjs/operators";
import {AuthenticationService} from "../services/authentication.service";
import {User} from "../model/user";
import {Subscription} from "rxjs";
import {ChatService} from "../services/chat.service";
import { UserService} from "../services/user.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  showHeader: boolean;
  emailVerified: boolean;
  user: User;
  showToast: boolean = false;
  private loggedInUserSubcription: Subscription;

  constructor(
    private activatedRoute: ActivatedRoute,
    private authenticationService: AuthenticationService,
    private chatService: ChatService,
    private router: Router,
    private titleService: Title,
    private userService: UserService
  ) {
  }

  ngOnInit(): void {
    this.trackPageViews();

    this.authenticationService.loggedInUser$.subscribe(
      (user) => {
        this.onChangedLogin(user);
        this.user = user;
        if (this.user?.id) {
          this.isEmailVerified(this.user.id);
        }
      }
    )

    this.subscribeForTitleChanges()
  }

  private onChangedLogin(user: User) {
    //Only show standard header if logged on (ie loggedInUser is not null)
    this.showHeader = user != null

    //If logged out...
    if (user == null) {
      this.onLogout();
    }
  }

  private onLogout() {
    this.chatService.cleanUp();
    this.router.navigate(['login']);
  }

  private subscribeForTitleChanges() {
    //Hook into router events in order to keep browser title updated based
    //on titles associated with various routes defined in app-routing.module.ts.

    //Hook into router events
    this.router.events.pipe(
      //Just interested in NavigationEnd events
      filter(event => event instanceof NavigationEnd),

      //Pass on the title string associated with the activated route.
      map(() => {
          //Default to the currently set title.
          const appTitle = this.titleService.getTitle();

          //Activated route is the path down through the Routes structure defined
          //in app-routing.module.ts
          let child = this.activatedRoute.firstChild;
          while (child.firstChild) {
            child = child.firstChild;
          }
          if (child.snapshot.data['title']) {
            return child.snapshot.data['title'];
          }
          //Return current title if we didn't find anything else.
          return appTitle;
        }
      )
    ).subscribe(
      (title: string) => {
        this.titleService.setTitle(title);
      }
    );
  }

  private isEmailVerified(userId: number): any {
    this.userService.get(userId).subscribe(
      (user) => {
        this.emailVerified = user.emailVerified;
        if (this.emailVerified === false) {
          this.showToast = true;
        }
      }
    );
  }


  /**
   * Tracks page views in a Single Page Application (SPA) context using Google Analytics.
   *
   * In traditional websites, navigation between pages naturally triggers a page load,
   * which Google Analytics uses to track page views. However, in SPAs like those built with Angular,
   * navigation changes the content dynamically without reloading the entire page. This function
   * subscribes to Angular Router events to detect when navigation ends and a new "page" is viewed,
   * manually sending page view information to Google Analytics.
   *
   * The `NavigationEnd` event indicates a successful route change, at which point we use the
   * `gtag` function with the 'config' command to send the current page path to Google Analytics.
   *
   * Additionally, console logs are included for testing purposes.
   *
   * See, for example, https://blog.mestwin.net/add-google-analytics-to-angular-application-in-3-easy-steps
   */
  private trackPageViews() {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        gtag('config', environment.googleAnalyticsId, {
          'page_path': event.urlAfterRedirects
        });
        // console.log('Sending Google Analytics tracking for: ', event.urlAfterRedirects);
        // console.log('Google Analytics property ID: ', environment.googleAnalyticsId);
      }
    });
  }
}
}
declare let gtag: Function;
