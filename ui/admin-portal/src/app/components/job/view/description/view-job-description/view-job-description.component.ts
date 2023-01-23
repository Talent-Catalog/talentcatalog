import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-description',
  templateUrl: './view-job-description.component.html',
  styleUrls: ['./view-job-description.component.scss']
})
export class ViewJobDescriptionComponent implements OnInit {
  @Input() job: Job;

  jobDescription = "https://static1.squarespace.com/static/5dc0262432cd095744bf1bf2/t/5f8f8928981fbd24aec3836d/1603242283119/TBB-Talent-Catalog-Privacy-Policy.pdf";
  // jobDescription= "https://drive.google.com/file/d/1i0FdQ-B2UXk0pU0CAd2fT6ddxLmFVNhg/view";
  constructor() { }

  ngOnInit(): void {
  }

}
