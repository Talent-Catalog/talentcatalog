import {Component, OnInit} from '@angular/core';
import {User} from "../../model/user";
import {AuthorizationService} from "../../services/authorization.service";
import {AuthenticationService} from "../../services/authentication.service";
import {LocalStorageService} from "../../services/local-storage.service";

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
}
