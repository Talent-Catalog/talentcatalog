import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../model/job";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {MainSidePanelBase} from "../../../util/split/MainSidePanelBase";
import {User} from "../../../../model/user";
import {AuthService} from "../../../../services/auth.service";
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent extends MainSidePanelBase implements OnInit {
  @Input() job: Job;

  activeTabId: string;
  loggedInUser: User;

  private lastTabKey: string = 'JobLastTab';

  constructor(
    private authService: AuthService,
    private localStorageService: LocalStorageService,
  ) {
    super(0,0, false)
  }

  ngOnInit(): void {
    this.loggedInUser = this.authService.getLoggedInUser();
    this.selectDefaultTab();
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.activeTabId = defaultActiveTabID;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  publishJob() {
    //todo
  }

}
