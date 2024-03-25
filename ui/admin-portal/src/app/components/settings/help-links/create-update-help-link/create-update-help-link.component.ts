import {Component, OnInit} from '@angular/core';
import {Country} from "../../../../model/country";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {User} from "../../../../model/user";
import {enumOptions} from "../../../../util/enum";
import {Status} from "../../../../model/base";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../../services/authorization.service";
import {CountryService} from "../../../../services/country.service";
import {UserService} from "../../../../services/user.service";
import {FormComponentBase} from "../../../util/form/FormComponentBase";
import {HelpLink, UpdateHelpLinkRequest} from "../../../../model/help-link";
import {HelpLinkService} from "../../../../services/help-link.service";

@Component({
  selector: 'app-create-update-help-link',
  templateUrl: './create-update-help-link.component.html',
  styleUrls: ['./create-update-help-link.component.scss']
})
export class CreateUpdateHelpLinkComponent extends FormComponentBase implements OnInit {

  destinationCountries: Country[];
  error = null;
  form: FormGroup;
  helpLink: HelpLink;
  partnerUsers: User[];
  statuses = enumOptions(Status);
  working: boolean;


  constructor(fb: FormBuilder,
              private activeModal: NgbActiveModal,
              private authorizationService: AuthorizationService,
              private countryService: CountryService,
              private helpLinkService: HelpLinkService,
              private userService: UserService,
  ) {
    super(fb);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      label: [this.helpLink?.label, Validators.required],
      link: [this.helpLink?.link, Validators.required],
      countryId: [this.helpLink?.countryId],
      caseStage: [this.helpLink?.caseStage],
      jobStage: [this.helpLink?.jobStage],
      focus: [this.helpLink?.focus],
      nextStepInfo: [this.helpLink?.nextStepInfo],
    });
  }

  get create(): boolean {
    return !this.helpLink;
  }

  get title(): string {
    return this.create ? "Add New Help Link"
      : "Update Help Link";
  }

  save() {
    this.error = null;
    this.working = true;

    const request: UpdateHelpLinkRequest = {
      label: this.form.value.label,
      link: this.form.value.link,
      countryId: this.form.value.countryId,
      caseStage: this.form.value.caseStage,
      jobStage: this.form.value.jobStage,
      focus: this.form.value.focus,
      nextStepInfo: this.form.value.nextStepInfo
    };

    if (this.create) {
      this.helpLinkService.create(request).subscribe(
        (helpLink: HelpLink) => {
          this.closeModal(helpLink);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });
    } else {
      this.helpLinkService.update(this.helpLink.id, request).subscribe(
        (helpLink: HelpLink) => {
          this.closeModal(helpLink);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });
    }
  }

  closeModal(helpLink: HelpLink) {
    this.activeModal.close(helpLink);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  isCreate(): boolean {
    return this.helpLink == null;
  }
}
