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

import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Title} from "@angular/platform-browser";
import {filter, map} from "rxjs/operators";
import {AuthenticationService} from "../services/authentication.service";
import {User} from "../model/user";
import {Subscription} from "rxjs";
import {ChatService} from "../services/chat.service";
import { Toast } from 'bootstrap';
import { UserService} from "../services/user.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  showHeader: boolean;
  emailVerified: boolean;
  userId: number = null;
  token: string;
  private loggedInUserSubcription: Subscription;

  @ViewChild('toastElement', { static: false }) toastElement!: ElementRef;

  constructor(
    private activatedRoute: ActivatedRoute,
    private authenticationService: AuthenticationService,
    private chatService: ChatService,
    private router: Router,
    private route: ActivatedRoute,
    private titleService: Title,
    private userService: UserService
  ) {
  }

  ngOnInit(): void {
    this.authenticationService.loggedInUser$.subscribe(
      (user) => {
        this.onChangedLogin(user);
        this.userId = user.id;
        if (this.userId) {
          this.isEmailVerified(this.userId);
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

  ngAfterViewInit() {
    if (!this.emailVerified && !this.token) {
      setTimeout(() => {
        if (this.toastElement) {
          const toast = new Toast(this.toastElement.nativeElement, {
            autohide: false // Disable auto-hide
          });
          if (!this.token) {
            toast.show();
          }
        }
      }, 500); // Small delay to ensure the DOM is ready
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

  openModal(modal: any) {
    modal.openModal();
  }

  private isEmailVerified(userId: number): any {
    this.userService.get(userId).subscribe(
      (user) => {
        this.emailVerified = user.emailVerified;
        this.token = this.route.snapshot.queryParamMap.get('token');
      },
      (error) => {
        this.emailVerified = false;
        console.error('Error fetching user data', error);
      }
    );
  }
}


