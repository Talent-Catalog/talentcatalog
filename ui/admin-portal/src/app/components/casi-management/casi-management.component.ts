/*
 * Copyright (c) 2026 Talent Catalog.
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
import {environment} from "../../../environments/environment";

@Component({
  selector: 'app-casi-management',
  templateUrl: './casi-management.component.html',
  styleUrls: ['./casi-management.component.scss']
})
export class CasiManagementComponent implements OnInit {
  loggedInUser: User;
  activeTabId: string;
  private lastTabKey = 'CasiManagementLastTab';

  constructor(
    private authService: AuthorizationService,
    private authenticationService: AuthenticationService,
    private localStorageService: LocalStorageService
  ) {
  }

  ngOnInit() {
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
    this.setActiveTabId(defaultActiveTabID == null ? "duolingo-coupons" : defaultActiveTabID);
  }

  systemAdminOnly(): boolean {
    return this.authService.isSystemAdminOnly();
  }

  isLocalEnv(): boolean {
    return environment.environmentName === 'local';
  }
}
