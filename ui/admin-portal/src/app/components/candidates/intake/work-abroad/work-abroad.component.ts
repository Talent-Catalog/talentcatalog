import {Component, Input, OnInit} from '@angular/core';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {Country} from '../../../../model/country';
import {IDropdownSettings} from 'ng-multiselect-dropdown';

@Component({
  selector: 'app-work-abroad',
  templateUrl: './work-abroad.component.html',
  styleUrls: ['./work-abroad.component.scss']
})
export class WorkAbroadComponent extends IntakeComponentBase implements OnInit {

  @Input() countries: Country[];

  /* MULTI SELECT */
  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    enableCheckAll: false,
    singleSelection: false,
    allowSearchFilter: true
  };


  public workAbroadOptions: EnumOption[] = enumOptions(YesNo);
  public selectedCountryIds: number[];
  public existingIds: number[];

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    // Get the countries from the ids
    let selectedCountries = [];
    if (this.candidateIntakeData?.workAbroadCountryIds && this.countries) {
      selectedCountries = this.countries.filter(c => this.candidateIntakeData?.workAbroadCountryIds.includes(c.id));
    } else {
      selectedCountries = [];
    }
    //this.form.controls['selectedCountries'].patchValue(selectedCountries);
    // if (this.candidateIntakeData?.workAbroadCountryIds != null) {
    //   this.existingIds = this.form.value.selectedCountries.map(c => c.id)
    // }

    this.form = this.fb.group({
      workAbroad: [this.candidateIntakeData?.workAbroad],
      workAbroadCountryIds: [],
      workAbroadYrs: [this.candidateIntakeData?.workAbroadYrs],
      // Used to get the ids from multiselect
      selectedCountries: [selectedCountries]
    });
  }

  get workAbroad(): string {
    return this.form.value?.workAbroad;
  }

  // Get Ids from the countries
  // getIdsMultiSelect(request): SearchCandidateRequestPaged {
  //   if (this.form.value.countries != null) {
  //     request.countryIds = request.countries.map(c => c.id);
  //     delete request.countries;
  //   }
  // }

  private updateSelectedIds() {
    const ids: number[] = this.form.value.selectedCountries.map(c => c.id);
    this.form.controls['workAbroadCountryIds'].setValue(ids);
    console.log('test');
  }

  private addIds() {
    this.updateSelectedIds()
  }

  private removeIds($event) {
    this.updateSelectedIds()
  }

}
