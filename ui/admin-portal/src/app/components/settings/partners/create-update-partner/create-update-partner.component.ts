import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {PartnerService} from "../../../../services/partner.service";
import {Partner, UpdatePartnerRequest} from "../../../../model/partner";
import {salesforceUrlPattern, Status} from "../../../../model/base";
import {Country} from "../../../../model/country";
import {CountryService} from "../../../../services/country.service";
import {enumOptions} from "../../../../util/enum";
import {FormComponentBase} from "../../../util/form/FormComponentBase";

/*
  MODEL - latest best practice on this kind of component

  - shows how to combine create and update into a single component, reducing unnecessary duplication

  - shows how to display arrays of objects in a drop down (countries), displaying one attribute
  (name) but just sending id's of the selected object(s) back down to the server. There is no point
  sending the whole objects back down. The server code will just want the id's so that they can
  retrieve fresh object details from the database using the id.

  - shows how to display enumerated type values in a drop down, then send the string key of the
  back to the server. Trick is to work with EnumOptions for display purposes in drop downs.

  NOTE - Why Enumerations are better than strings

  - Enumerations add "type safety", eliminating a whole range of errors coming from typing errors
  when typing in strings - eg "admni" instead of "admin". With enum's those sort of typos are
  picked up at compile time. Role.admni doesn't exist and will cause a compile error.

  - Enumerations make coding quicker because IDE's like Intellij can automatically prompt you for
  the value you want. For example, just type "Role." and Intellij will prompt you for all legal
  values.
 */

@Component({
  selector: 'app-create-update-partner',
  templateUrl: './create-update-partner.component.html',
  styleUrls: ['./create-update-partner.component.scss']
})
export class CreateUpdatePartnerComponent extends FormComponentBase implements OnInit {

  countries: Country[];
  error = null;
  form: FormGroup;
  partner: Partner;
  statuses = enumOptions(Status);
  working: boolean;


  constructor(fb: FormBuilder,
              private activeModal: NgbActiveModal,
              private countryService: CountryService,
              private partnerService: PartnerService) {
    super(fb);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      abbreviation: [this.partner?.abbreviation, Validators.required],
      defaultPartnerRef: [this.partner?.defaultPartnerRef],
      logo: [this.partner?.logo],
      name: [this.partner?.name, Validators.required],
      notificationEmail: [this.partner?.notificationEmail],
      registrationLandingPage: [this.partner?.registrationLandingPage],
      registrationDomain: [this.partner?.registrationDomain],
      sflink: [this.partner?.sflink, [Validators.pattern(salesforceUrlPattern)]],
      sourceCountries: [this.partner?.sourceCountries],

      //Note that the value passed in here is the string key of the enum. In the html the
      //of the drop down the EnumOptions of the enum is passed in as allowable value
      //(see statuses above) and the drop down is configured with the "bindValue" set to the
      //enum key and the "bindLabel" set to the enum stringValue (which is what is displayed to
      //the user).
      status: [this.partner?.status, Validators.required],
      websiteUrl: [this.partner?.websiteUrl],
    });

    this.countryService.listCountriesRestricted().subscribe(
      (response) => {
        this.countries = response;
      },
      (error) => {
        this.error = error;
        this.working = false;
      }
    );

  }

  get create(): boolean {
    return !this.partner;
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
      defaultPartnerRef: this.form.value.defaultPartnerRef,
      logo: this.form.value.logo,
      name: this.form.value.name,
      notificationEmail: this.form.value.notificationEmail,
      partnerType: 'SourcePartner',
      registrationLandingPage: this.form.value.registrationLandingPage,
      registrationDomain: this.form.value.registrationDomain,
      sflink: this.form.value.sflink,

      //Convert countries to country ids
      sourceCountryIds: this.form.value.sourceCountries?.map(c => c.id),

      //The for status contains the key of the associated enum. On the server side, that field
      //of the corresponding Java UpdatePartnerRequest will be typed as the Java enum Status.
      //The JSON processing code automatically converts the string value sent to the corresponding
      //Status Java enumeration value.
      status: this.form.value.status,

      websiteUrl: this.form.value.websiteUrl,

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

}
