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
import {User} from "../../model/user";
import {AuthorizationService} from "../../services/authorization.service";
import {AuthenticationService} from "../../services/authentication.service";
import {LocalStorageService} from "../../services/local-storage.service";


@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

  loggedInUser: User;
  activeTabId: string;
  private lastTabKey: string = 'SettingsLastTab';

  constructor(
    private authService: AuthorizationService,
    private authenticationService: AuthenticationService,
    private localStorageService: LocalStorageService
  ) { }

  ngOnInit(){
    /* GET LOGGED IN USER ROLE FROM LOCAL STORAGE */
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    this.selectDefaultTab();
  }

  onTabChanged(nextTab: string) {
    this.setActiveTabId(nextTab);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.setActiveTabId(defaultActiveTabID == null ? "users" : defaultActiveTabID);
  }

  systemAdminOnly(): boolean {
    return this.authService.isSystemAdminOnly();
  }

  canSeeExternalLinksAndTasks(): boolean {
    return this.authService.canManageCandidateTasks();
  }
}
