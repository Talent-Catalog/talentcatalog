import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";
import {PartnerService} from "../../../../../services/partner.service";
import {
  Partner,
  sourceCountriesAsString,
  UpdatePartnerJobContactRequest
} from "../../../../../model/partner";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {User} from "../../../../../model/user";
import {UserService} from "../../../../../services/user.service";
import {SearchUserRequest} from "../../../../../model/base";
import {
  HasNameSelectorComponent
} from "../../../../util/has-name-selector/has-name-selector.component";
import {AuthService} from "../../../../../services/auth.service";
import {AuthenticationService} from "../../../../../services/authentication.service";

/*
MODEL: Modal popups.
 */
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
  private loggedInUserPartnerId: number;

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private partnerService: PartnerService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.loggedInUserPartnerId = this.authenticationService.getLoggedInUser()?.partner?.id;

    this.error = null;
    this.loading = true;
    this.partnerService.listSourcePartners(this.job).subscribe(
      (sourcePartners) => {this.sourcePartners = sourcePartners; this.loading = false},
      (error) => {this.error = error; this.loading = false}
    )
  }

  editPartnerContact(partner: Partner) {
    //Get users for given partner.
    const request: SearchUserRequest = {
      partnerId: partner.id,
      sortFields: ["firstName", "lastName"],
      sortDirection: "ASC"
    }
    this.error = null;
    this.loading = true;
    this.userService.search(request)
    .subscribe(
      (users) => {this.loading = false; this.selectPartnerContactUser(partner, users)},
      (error) => {this.error = error; this.loading = false},
    );
  }

  /**
   * Select partner contact from drop down of given users
   */
  private selectPartnerContactUser(partner: Partner, users: User[]) {
    users.forEach(user => user.name = user.firstName + " " + user.lastName)
    const selectUserModal = this.modalService.open(HasNameSelectorComponent, {
      centered: true,
      backdrop: 'static'
    });
    selectUserModal.componentInstance.label = "Select user to contact";
    selectUserModal.componentInstance.hasNames = users;
    selectUserModal.result.then(
      (user: User) => {this.updateContact(partner, user)},
    )
    .catch(() => {})
  }

  /**
   * Update the given partners contact user for this job
   * @param partner Partner
   * @param user Contact user
   */
  private updateContact(partner: Partner, user: User) {
    const request: UpdatePartnerJobContactRequest = {
      jobId: this.job.id,
      userId: user.id
    }
    this.error = null;
    this.loading = true;
    this.partnerService.updateJobContact(partner.id, request).subscribe(
      (partner) => {this.updateSourcePartners(partner); this.loading = false},
      error => {this.error = error; this.loading = false}
    );
  }

  /**
   * Update given partner in our array of source partners
   * @param partner Updated partner
   */
  private updateSourcePartners(partner: Partner) {
    let modifiedPartnerIndex = this.sourcePartners.findIndex(p => p.id === partner.id );
    if (modifiedPartnerIndex >= 0) {
      this.sourcePartners[modifiedPartnerIndex] = partner;
    } else {
      console.log("Bug - partner " + partner.id + " not found in source partners")
    }
  }

  isEditable(partner: Partner): boolean {
    let canEdit: boolean = this.editable;
    if (canEdit) {
      canEdit = this.loggedInUserPartnerId === partner.id;
    }
    return canEdit;
  }

  sourceCountries(partner: Partner) {
    let ret = "";
    const s = sourceCountriesAsString(partner);
    if (s) {
      ret = "(" + s + ")"
    }
    return ret;
  }
}
