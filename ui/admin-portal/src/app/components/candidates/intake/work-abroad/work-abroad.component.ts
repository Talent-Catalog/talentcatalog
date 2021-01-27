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

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      workAbroad: [this.candidateIntakeData?.workAbroad],
      workAbroadCountryIds: [this.candidateIntakeData?.workAbroadCountryIds],
      workAbroadYrs: [this.candidateIntakeData?.workAbroadYrs],
      countriesSelected: [],
    });
  }
  get workAbroad(): string {
    return this.form.value?.workAbroad;
  }

  private getIdsMultiSelect(request) {
    if (this.form.value.countriesSelected != null) {
      this.form.value.countryIds = request.countries.map(c => c.id);
      delete request.countries;
    }
  }

}
