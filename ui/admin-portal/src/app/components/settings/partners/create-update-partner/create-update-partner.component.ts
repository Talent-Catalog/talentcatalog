/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  AbstractControl,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {PartnerService} from "../../../../services/partner.service";
import {Partner, PublicApiAuthority, UpdatePartnerRequest} from "../../../../model/partner";
import {
  salesforceSandboxUrlPattern,
  salesforceUrlPattern,
  SearchUserRequest,
  Status
} from "../../../../model/base";
import {Country} from "../../../../model/country";
import {CountryService} from "../../../../services/country.service";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {FormComponentBase} from "../../../util/form/FormComponentBase";
import {User} from "../../../../model/user";
import {UserService} from "../../../../services/user.service";

/* MODEL - Cross field validation in a form.
   See crossFieldValidator function.
*/

/*
  MODEL - mapping enums, display text send ids, create/update component

  - shows how to combine create and update into a single component, reducing unnecessary duplication

  - shows how to display arrays of objects in a drop down (countries), displaying one attribute
  (name) but just sending id's of the selected object(s) back down to the server. There is no point
  sending the whole objects back down. The server code will just want the id's so that they can
  retrieve fresh object details from the database using the id.

  - shows how to display enumerated type values in a drop down, then send the string key of the
  enum back to the server. Trick is to work with EnumOptions for display purposes in drop downs.

  NOTE - Why Enumerations are better than strings

  - Enumerations add "type safety", eliminating a whole range of errors coming from typing errors
  when typing in strings - eg "admni" instead of "admin". With enum's those sort of typos are
  picked up at compile time. Role.admni doesn't exist and will cause a compile error.

  - Enumerations make coding quicker because IDE's like Intellij can automatically prompt you for
  the value you want. For example, just type "Role." and Intellij will prompt you for all legal
  values.
 */

/**
 * Cross field validator - configured into above form definition.
 * <p/>
 * See https://angular.io/guide/form-validation#cross-field-validation
 * <p/>
 * Checks that employer partners always have a salesforce link.
 * <p/>
 * See also div in html file which checks form.errors
 * </p>
 * Note that this isn't be a method. Actually you can make it a method and it sort of works
 * but it doesn't see class fields so best to define it as recommended by Angular above.
 * @param control The form group will be passed in here.
 * @private
 * @return Null if no errors otherwise object containing fields for errors:
 * missingEmployerPartnerSflink and sourcePartnerIsJobCreator
 */
export const crossFieldValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  let errors = null;
  const defaultSourcePartnerCtrl = control.get('defaultSourcePartner');
  const employerPartnerCtrl = control.get('employerPartner');
  const jobCreatorCtrl = control.get('jobCreator');
  const sourcePartnerCtrl = control.get('sourcePartner');
  const sflinkCtrl = control.get('sflink');
  if (employerPartnerCtrl.value) {
    const sflink = sflinkCtrl.value;
    if (!sflink) {
      errors = {missingEmployerPartnerSflink: true}
    }
  }
  if (!defaultSourcePartnerCtrl.value) {
    if (sourcePartnerCtrl.value) {
      if (jobCreatorCtrl.value) {
        errors = {sourcePartnerIsJobCreator: true}
      }
    }
  }
  return errors;
}

@Component({
  selector: 'app-create-update-partner',
  templateUrl: './create-update-partner.component.html',
  styleUrls: ['./create-update-partner.component.scss']
})
export class CreateUpdatePartnerComponent extends FormComponentBase implements OnInit {

  countries: Country[];
  partners: Partner[];
  error = null;
  form: UntypedFormGroup;
  partner: Partner;
  partnerUsers: User[];
  statuses = enumOptions(Status);
  publicApiAuthorityOptions: EnumOption[] = enumOptions(PublicApiAuthority);

  working: boolean;


  constructor(fb: UntypedFormBuilder,
              private activeModal: NgbActiveModal,
              private countryService: CountryService,
              private partnerService: PartnerService,
              private userService: UserService,
  ) {
    super(fb);
  }

  ngOnInit(): void {
    const defaultContact = this.partner?.defaultContact;
    if (defaultContact) {
      defaultContact.name = defaultContact.firstName + " " + defaultContact.lastName
    }
    this.form = this.fb.group({
      abbreviation: [this.partner?.abbreviation, Validators.required],
      autoAssignable: [this.partner?.autoAssignable],
      defaultContact: [this.partner?.defaultContact],
      defaultPartnerRef: [this.partner?.defaultPartnerRef],

      //This is never displayed so can't be modified - it is just is here to let the validator know
      //whether this is the special TBB partner (which CAN be a job creator AND a source partner).
      defaultSourcePartner: [this.partner?.defaultSourcePartner],

      employerPartner: [this.partner?.employer ? true : false],
      jobCreator: [this.partner?.jobCreator],
      logo: [this.partner?.logo],
      name: [this.partner?.name, Validators.required],
      notificationEmail: [this.partner?.notificationEmail],
      publicApiAccess: [this.partner?.publicApiAccess],
      publicApiAuthorities: [this.partner?.publicApiAuthorities],

      registrationLandingPage: [this.partner?.registrationLandingPage],
      sflink: [this.partner?.sflink, [Validators.pattern(`${salesforceUrlPattern}|${salesforceSandboxUrlPattern}`)]],
      sourceCountries: [this.partner?.sourceCountries],
      sourcePartner: [this.partner?.sourcePartner],

      //Note that the value passed in here is the string key of the enum. In the html the
      //of the drop down the EnumOptions of the enum is passed in as allowable value
      //(see statuses above) and the drop down is configured with the "bindValue" set to the
      //enum key and the "bindLabel" set to the enum stringValue (which is what is displayed to
      //the user).
      status: [this.partner?.status, Validators.required],
      websiteUrl: [this.partner?.websiteUrl],
      redirectPartnerId: [this.partner?.redirectPartner?.id],
    },{validators: crossFieldValidator});

    this.countryService.listCountriesRestricted().subscribe(
      (response) => {
        this.countries = response;
        this.working = false
      },
      (error) => {
        this.error = error;
        this.working = false
      }
    );

    this.partnerService.listPartners().subscribe(
      (response) => {
        this.partners = response;
        this.working = false
      },
      (error) => {
        this.error = error;
        this.working = false
      }
    );

    if (this.partner) {
      //Get users for given partner.
      const request: SearchUserRequest = {
        partnerId: this.partner.id,
        sortFields: ["firstName", "lastName"],
        sortDirection: "ASC"
      }
      this.error = null;
      this.working = true;
      this.userService.search(request).subscribe(
        (users) => {
          this.working = false;
          users.forEach(user => user.name = user.firstName + " " + user.lastName);
          this.partnerUsers = users
        },
        (error) => {
          this.error = error;
          this.working = false
        },
      );
    }
  }

  get create(): boolean {
    return !this.partner;
  }

  get publicApiAccess(): boolean {
    return this.form.value.publicApiAccess;
  }

  get title(): string {
    return this.create ? "Add New Partner"
      : "Update Partner";
  }

  save() {
    this.error = null;
    this.working = true;

    const request: UpdatePartnerRequest = {
      abbreviation: this.form.value.abbreviation,
      autoAssignable: this.form.value.autoAssignable,
      defaultContactId: this.form.value.defaultContact?.id,
      defaultPartnerRef: this.form.value.defaultPartnerRef,
      employerSflink: this.form.value.employerPartner ? this.form.value.sflink : null,
      logo: this.form.value.logo,
      name: this.form.value.name,
      notificationEmail: this.form.value.notificationEmail,
      publicApiAccess: this.form.value.publicApiAccess,
      publicApiAuthorities: this.form.value.publicApiAuthorities,
      jobCreator: this.form.value.jobCreator,
      registrationLandingPage: this.form.value.registrationLandingPage,
      sflink: this.form.value.sflink,

      //Convert countries to country ids
      sourceCountryIds: this.form.value.sourceCountries?.map(c => c.id),
      sourcePartner: this.form.value.sourcePartner,

      //The form status contains the key of the associated enum. On the server side, that field
      //of the corresponding Java UpdatePartnerRequest will be typed as the Java enum Status.
      //The JSON processing code automatically converts the string value sent to the corresponding
      //Status Java enumeration value.
      status: this.form.value.status,

      websiteUrl: this.form.value.websiteUrl,
      redirectPartnerId: this.form.value.redirectPartnerId
    };

    if (this.create) {
      this.partnerService.create(request).subscribe(
        (partner: Partner) => {
          this.closeModal(partner);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });
    } else {
      this.partnerService.update(this.partner.id, request).subscribe(
        (partner: Partner) => {
          this.closeModal(partner);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });
    }
  }

  closeModal(partner: Partner) {
    this.activeModal.close(partner);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  isSourcePartner(): boolean {
    return this.form.value.sourcePartner;
  }

  isCreate(): boolean {
    return this.partner == null;
  }
}
