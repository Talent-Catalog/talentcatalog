import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../model/job";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent implements OnInit {
  @Input() job: Job;

  activeTabId: string;

  mainColWidth = 8;
  sidePanelColWidth = 4;

  constructor() { }

  ngOnInit(): void {
  }


  onTabChanged(event: NgbNavChangeEvent) {
    //todo
  }

  resizeSidePanel() {
    this.mainColWidth = this.mainColWidth === 8 ? this.mainColWidth + 2 : this.mainColWidth - 2;
    this.sidePanelColWidth = this.mainColWidth === 10 ? this.sidePanelColWidth - 2 : this.sidePanelColWidth + 2;
  }


}
