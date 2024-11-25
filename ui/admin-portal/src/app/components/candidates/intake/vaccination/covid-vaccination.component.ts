import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {VaccinationStatus, YesNo} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-covid-vaccination',
  templateUrl: './covid-vaccination.component.html',
  styleUrls: ['./covid-vaccination.component.scss']
})
export class CovidVaccinationComponent extends IntakeComponentBase implements OnInit {

  public vaccinationOptions: EnumOption[] = enumOptions(YesNo);
  public vaccinationStatusOptions: EnumOption[] = enumOptions(VaccinationStatus);

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      covidVaccinated: [this.candidateIntakeData?.covidVaccinated],
      covidVaccinatedStatus: [this.candidateIntakeData?.covidVaccinatedStatus],
      covidVaccinatedDate: [this.candidateIntakeData?.covidVaccinatedDate],
      covidVaccineName: [this.candidateIntakeData?.covidVaccineName],
      covidVaccineNotes: [this.candidateIntakeData?.covidVaccineNotes],
    });
  }

  get covidVaccinated(): string {
    return this.form.value?.covidVaccinated;
  }

}
