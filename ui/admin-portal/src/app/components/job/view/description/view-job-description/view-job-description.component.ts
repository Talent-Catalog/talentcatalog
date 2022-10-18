import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-description',
  templateUrl: './view-job-description.component.html',
  styleUrls: ['./view-job-description.component.scss']
})
export class ViewJobDescriptionComponent implements OnInit {
  @Input() job: Job;
  @Output() resizeEvent = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

}
