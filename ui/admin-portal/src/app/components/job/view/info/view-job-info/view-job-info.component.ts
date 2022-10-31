import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {User} from "../../../../../model/user";

@Component({
  selector: 'app-view-job-info',
  templateUrl: './view-job-info.component.html',
  styleUrls: ['./view-job-info.component.scss']
})
export class ViewJobInfoComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  constructor(private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  editContactDetails() {
    const editModal = this.modalService.open(EditJobInfoComponent, {
      centered: true,
      backdrop: 'static'
    });

    editModal.componentInstance.jobId = this.job.id;

    editModal.result
    .then((job) => this.job = job)
    .catch(() => {});

  }

  showUser(contactUser: User): string {
    return contactUser ?
      contactUser.firstName + " " + contactUser.lastName + " " + contactUser.email
      : "";
  }
}
