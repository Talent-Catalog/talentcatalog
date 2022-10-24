import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../model/job";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {MainSidePanelBase} from "../../../util/split/MainSidePanelBase";

@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent extends MainSidePanelBase implements OnInit {
  @Input() job: Job;

  activeTabId: string;

  constructor() {
    super(4,6, false)
  }

  ngOnInit(): void {
  }


  onTabChanged(event: NgbNavChangeEvent) {
    //todo
  }

}
