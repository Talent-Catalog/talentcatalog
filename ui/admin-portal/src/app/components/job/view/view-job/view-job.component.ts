import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../model/job";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {MainSidePanelBase} from "../../../util/split/MainSidePanelBase";
import {User} from "../../../../model/user";
import {AuthService} from "../../../../services/auth.service";

@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent extends MainSidePanelBase implements OnInit {
  @Input() job: Job;

  activeTabId: string;
  loggedInUser: User;

  constructor(private authService: AuthService) {
    super(0,0, false)
  }

  ngOnInit(): void {
    this.loggedInUser = this.authService.getLoggedInUser();
  }


  onTabChanged(event: NgbNavChangeEvent) {
    //todo
  }

}
