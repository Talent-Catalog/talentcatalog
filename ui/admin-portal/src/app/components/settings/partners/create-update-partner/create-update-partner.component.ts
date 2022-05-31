import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {PartnerService} from "../../../../services/partner.service";
import {Partner, UpdatePartnerRequest} from "../../../../model/partner";
import {Status} from "../../../../model/base";
import {Country} from "../../../../model/country";
import {CountryService} from "../../../../services/country.service";
import {enumOptions} from "../../../../util/enum";

/*
  MODEL - latest best practice on this kind of component

  - shows how to combine create and update into a single component, reducing unnecessary duplication

  - shows how to display arrays of objects in a drop down (countries), displaying one attribute
  (name) but just sending id's of the selected object(s) back down to the server. There is no point
  sending the whole objects back down. The server code will just want the id's so that they can
  retrieve fresh object details from the database using the id.

  - shows how to display enumerated type values in a drop down, then send an enumerated type value
  back to the server. Trick is to work with EnumOptions everywhere - converting to the real Enum
  just prior to sending to server.

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
export class CreateUpdatePartnerComponent implements OnInit {

  countries: Country[];
  error = null;
  form: FormGroup;
  partner: Partner;
  statuses = enumOptions(Status);
  working: boolean;


  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private countryService: CountryService,
              private partnerService: PartnerService) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      abbreviation: [this.partner?.abbreviation],
      logo: [this.partner?.logo],
      name: [this.partner?.name, Validators.required],
      registrationLandingPage: [this.partner?.registrationLandingPage],
      registrationDomain: [this.partner?.registrationDomain],
      sourceCountries: [this.partner?.sourceCountries],

      //Note that if you initialize with the actual Enum directly, ng-select will display the
      //string value of that enum if it finds one. Basically means that you don't need to
      //convert to EnumOption here - but can pass enumeration in directly.
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
      logo: this.form.value.logo,
      name: this.form.value.name,
      partnerType: 'SourcePartner',
      registrationLandingPage: this.form.value.registrationLandingPage,
      registrationDomain: this.form.value.registrationDomain,

      //Convert countries to country ids
      sourceCountryIds: this.form.value.sourceCountries?.map(c => c.id),

      //Convert returned enum key to Status enum.
      //Note that code below is a bit more complicated than needed. The following would work:
      //
      //     status: this.form.value.status
      //
      //This is because the form value is typed as an "any" and so it will
      //assign directly to the status attribute even though the status attribute is an Enum and
      //the form value is actually a string. Type checking is disabled for "any"s and a string
      //value is what actually gets sent in the JSON version of the request, even if an Enum is
      //assigned.
      //
      //But it is more correct to assign an Enum, and if we do that the code will continue
      //to work even if stronger type checking is enabled one day (by turning on "noImplicitAny"
      //- see https://www.typescriptlang.org/tsconfig#noImplicitAny )
      status: Status[this.form.value.status as string],

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
