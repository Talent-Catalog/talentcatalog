/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import { Component, HostBinding, OnInit } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { SwPush } from "@angular/service-worker";

import { LanguageService } from "../services/language.service";
import { LanguageLoader } from "../services/language.loader";
import { AuthenticationService } from "../services/authentication.service";
import { User } from "../model/user";
import { NavigationEnd, Router } from "@angular/router";
import { ChatService } from "../services/chat.service";
import { environment } from "../../environments/environment";
import { FcmService } from "../services/fcm.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
})
export class AppComponent implements OnInit {
  //This CSS setting is used at the root of the whole app
  @HostBinding("class.rtl-wrapper") rtl: boolean = false;

  loading: boolean;

  constructor(
    private translate: TranslateService,
    private authenticationService: AuthenticationService,
    private chatService: ChatService,
    private router: Router,
    private languageLoader: LanguageLoader,
    private languageService: LanguageService,
    private swPush: SwPush,
    private fcmService: FcmService
  ) {
    this.listenForMessages();
  }

  ngOnInit(): void {
    // this.requestPermission();
    this.subscribeToNotifications();
    this.requestPermission();
    // this.fcmService.listenForMessages();
    // this.swPush.messages.subscribe((message: any) => {
    //   console.log('Push message received', message);

    //   if (Notification.permission === 'granted') {
    //     new Notification(message.title, {
    //       body: message.body,
    //       icon: 'assets/icons/icon-72x72.png' // Replace with the path to your app's icon
    //     });
    //   }
    // });

    this.trackPageViews();

    this.authenticationService.loggedInUser$.subscribe((user) => {
      this.onChangedLogin(user);
    });

    //Register for language translation upload start and end events - which
    //drive the loading status.
    LanguageLoader.languageLoading$.subscribe((loading: boolean) => {
      this.loading = loading;
    });

    //Register for language change events which are used to set the language and
    //appropriate Right to Left direction. That can only be set in this
    //component.
    this.languageService.languageChanged$.subscribe(() => {
      this.translate.use(this.languageService.getSelectedLanguage());
      this.rtl = this.languageService.isSelectedLanguageRtl();
    });

    // this language will be used as a fallback when a translation isn't
    // found in the current language. This forces loading of translations.
    this.translate.setDefaultLang("en");

    this.translate.use("en");
  }

  listenForMessages() {
    // Firebase Notification
    this.fcmService.getMessages().subscribe(async (msg: any) => {
      if (Notification.permission === "granted") {
        new Notification(msg.notification.title, {
          body: msg.notification.body,
          icon: "assets/icons/icon-72x72.png",
        });
      }
    });
  }

  requestPermission() {
    this.fcmService.requestToken().subscribe(
      async (token) => {
        console.log("FCM Token:", token);
      },
      async (err) => {
        console.log("Fetching FCM token failed: ", err);
      }
    );
  }

  private subscribeToNotifications() {
    // Application Notifications
    this.swPush.messages.subscribe((message: any) => {
      if (Notification.permission === "granted") {
        new Notification(message.notification.title, {
          body: message.notification.body,
          icon: "assets/icons/icon-72x72.png",
        });
      }
    });
  }

  // private async requestPermission() {
  //   this.fireMessaging.requestToken.subscribe({
  //     next: token => {
  //       console.log(token)
  //     },
  //     error: err => {
  //       console.error('Fetching FCM token failed: ', +err)
  //     }
  // })
  // const messaging = getMessaging();
  // try {
  //   const permission = await Notification.requestPermission();
  //   if (permission === 'granted') {
  //     console.log('Notification permission granted.');
  //     const token = await getToken(messaging, {vapidKey: environment.firebase.vapidKey});
  //     console.log('Notification token:', token);
  //   } else {
  //     console.log('Unable to get permission to notify.');
  //   }
  // } catch (error) {
  //   console.error('Unable to get permission to notify.', error);
  // }
  // }

  private onChangedLogin(user: User) {
    //If logged out
    if (user == null) {
      this.onLogout();
    }
  }

  private onLogout() {
    this.chatService.cleanUp();
    //Show login screen
    this.router.navigate(["login"]);
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
  trackPageViews() {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        gtag("config", environment.googleAnalyticsId, {
          page_path: event.urlAfterRedirects,
        });
        // console.log('Sending Google Analytics tracking for: ', event.urlAfterRedirects);
        // console.log('Google Analytics property ID: ', environment.googleAnalyticsId);
      }
    });
  }
}

declare let gtag: Function;
