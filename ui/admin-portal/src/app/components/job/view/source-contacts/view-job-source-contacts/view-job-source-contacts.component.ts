import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";
import {PartnerService} from "../../../../../services/partner.service";
import {Partner} from "../../../../../model/partner";

@Component({
  selector: 'app-view-job-source-contacts',
  templateUrl: './view-job-source-contacts.component.html',
  styleUrls: ['./view-job-source-contacts.component.scss']
})
export class ViewJobSourceContactsComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  error: any;
  loading: boolean;
  sourcePartners: Partner[];

  constructor(
    private partnerService: PartnerService,
  ) { }

  ngOnInit(): void {
    this.error = null;
    this.loading = true;
    this.partnerService.listSourcePartners().subscribe(
      (sourcePartners) => {this.sourcePartners = sourcePartners; this.loading = false},
      (error) => {this.error = error; this.loading = false}
    )
  }

  editPartnerContact(partner: Partner) {
    //todo select from drop down of partner users
    //todo Update job, partner, contact user
  }
}
