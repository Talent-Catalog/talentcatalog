import {Component, OnInit} from '@angular/core';
import {CandidateService} from "../../../../../services/candidate.service";
import {NationalityService} from "../../../../../services/nationality.service";
import {IntakeComponentTabBase} from "../../../../util/intake/IntakeComponentTabBase";
import {FormBuilder, FormGroup} from "@angular/forms";
import {CountryService} from "../../../../../services/country.service";
import {
  CandidateVisaCheckService,
  CreateCandidateVisaCheckRequest
} from "../../../../../services/candidate-visa-check.service";
import {Country} from "../../../../../model/country";
import {HasNameSelectorComponent} from "../../../../util/has-name-selector/has-name-selector.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-candidate-visa-tab',
  templateUrl: './candidate-visa-tab.component.html',
  styleUrls: ['./candidate-visa-tab.component.scss']
})
export class CandidateVisaTabComponent
  extends IntakeComponentTabBase implements OnInit {
  destinations: Country[];
  form: FormGroup;
  selectedIndex: number;
  selectedCountry: string;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              nationalityService: NationalityService,
              private candidateVisaCheckService: CandidateVisaCheckService,
              private modalService: NgbModal,
              private fb: FormBuilder) {
    super(candidateService, countryService, nationalityService)
  }

  onDataLoaded(init: boolean) {
    if (init) {
      //If we have some visa checks, select the first one
      if (this.candidateIntakeData?.candidateVisaChecks.length > 0) {
        this.selectedIndex = 0;
      }
      this.form = this.fb.group({
        visaCountry: [this.selectedIndex]
      });

      this.changeVisaCountry(null);

      //todo debugging
      this.destinations = this.countries;
    }
  }

  /**
   * Filters out destinations already used in existingRecords
   */
  private get filteredDestinations(): Country[] {
    if (!this.destinations) {
      return [];
    } else if (!this.candidateIntakeData.candidateCitizenships) {
      return this.destinations;
    } else {
      const existingIds: number[] =
        this.candidateIntakeData.candidateVisaChecks.map(record => record.country?.id);
      return this.destinations.filter(
        record =>
          //Exclude already used ids
          !existingIds.includes(record.id)
      );
    }
  }

  addRecord() {
    const modal = this.modalService.open(HasNameSelectorComponent);
    modal.componentInstance.hasNames = this.destinations;
    modal.componentInstance.label = "TBB Destinations";

    modal.result
      .then((selection: Country) => {
        if (selection) {
          this.createRecord(selection);
        }
      })
      .catch(() => {
        //User cancelled selection
      });
  }

  createRecord(country: Country) {
    this.loading = true;
    const request: CreateCandidateVisaCheckRequest = {
      countryId: country.id
    };
    this.candidateVisaCheckService.create(this.candidate.id, request)
      .subscribe(
      (visaCheck) => {
        this.candidateIntakeData.candidateVisaChecks.push(visaCheck)
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

  }

  deleteRecord(i: number) {
    this.candidateIntakeData.candidateVisaChecks.splice(i, 1);
  }

  changeVisaCountry(event: Event) {
    this.selectedIndex = this.form.controls.visaCountry.value;
    this.selectedCountry = this.candidateIntakeData.candidateVisaChecks[this.selectedIndex].country?.name;
  }

}
