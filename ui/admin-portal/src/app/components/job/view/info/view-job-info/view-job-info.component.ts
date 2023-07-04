import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job} from "../../../../../model/job";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {User} from "../../../../../model/user";
import {SavedList} from "../../../../../model/saved-list";
import {
  JobPrepDueDate,
  JobPrepItem,
  JobPrepSuggestedCandidates
} from "../../../../../model/job-prep-item";

@Component({
  selector: 'app-view-job-info',
  templateUrl: './view-job-info.component.html',
  styleUrls: ['./view-job-info.component.scss']
})
export class ViewJobInfoComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;
  @Input() highlightItem: JobPrepItem;
  @Output() jobUpdated = new EventEmitter<Job>();

  constructor(private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  editJobInfo() {
    const editModal = this.modalService.open(EditJobInfoComponent, {
      centered: true,
      backdrop: 'static'
    });

    editModal.componentInstance.job = this.job;

    editModal.result
    .then((job) => this.jobUpdated.emit(job))
    .catch(() => {});

  }

  showUser(contactUser: User): string {
    return contactUser ?
      contactUser.firstName + " " + contactUser.lastName  : "";
  }

  displaySavedList(list: SavedList): string {
    return list ? list.name + "(" + list.id + ")" : "";
  }

  showEmailUrl(email: string) {
    return email ? "mailto:" + email : "";
  }

  isSpecialContactEmail(): boolean {
    let isSpecial = false;
    if (this.job.contactEmail) {
      if (this.job.contactUser?.email) {
        //We have a contact user email - so job contact email is only special if it is different
        isSpecial = this.job.contactEmail !== this.job.contactUser.email;
      } else {
        //No contact user or no contact user email. Contact email is all we have, so it is special
        isSpecial = true;
      }
    }
    return isSpecial;
  }

  highlightCandidates() {
    return this.highlightItem instanceof JobPrepSuggestedCandidates;
  }

  highlightSubmissionDate() {
    return this.highlightItem instanceof JobPrepDueDate;
  }
}
