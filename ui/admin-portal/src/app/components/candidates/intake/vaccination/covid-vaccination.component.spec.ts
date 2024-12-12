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
import {CovidVaccinationComponent} from "./covid-vaccination.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {DatePickerComponent} from "../../../util/date-picker/date-picker.component";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateService} from "../../../../services/candidate.service";
import {VaccinationStatus, YesNo} from "../../../../model/candidate";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('CovidVaccinationComponent', () => {
  let component: CovidVaccinationComponent;
  let fixture: ComponentFixture<CovidVaccinationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CovidVaccinationComponent, DatePickerComponent, AutosaveStatusComponent ], // Include the date picker component
      imports: [HttpClientTestingModule, NgbDatepickerModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CovidVaccinationComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      covidVaccinated: YesNo.Yes,
      covidVaccinatedStatus: VaccinationStatus.Full,
      covidVaccinatedDate: '2021-08-15',
      covidVaccineName: 'Pfizer',
      covidVaccineNotes: 'No side effects.'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('covidVaccinated').value).toBe(YesNo.Yes);
    expect(component.form.get('covidVaccinatedStatus').value).toBe(VaccinationStatus.Full);
    expect(component.form.get('covidVaccinatedDate').value).toBe('2021-08-15');
    expect(component.form.get('covidVaccineName').value).toBe('Pfizer');
    expect(component.form.get('covidVaccineNotes').value).toBe('No side effects.');
  });

  it('should display the covidVaccinatedStatus select when covidVaccinated is Yes', () => {
    component.form.get('covidVaccinated').setValue('Yes');
    fixture.detectChanges();
    const covidVaccinatedStatusSelect = fixture.nativeElement.querySelector('#covidVaccinatedStatus');
    expect(covidVaccinatedStatusSelect).toBeTruthy();
  });

  it('should display the covidVaccinatedDate date picker when covidVaccinated is Yes', () => {
    component.form.get('covidVaccinated').setValue('Yes');
    fixture.detectChanges();
    const covidVaccinatedDateDatePicker = fixture.nativeElement.querySelector('#covidVaccinatedDate');
    expect(covidVaccinatedDateDatePicker).toBeTruthy();
  });

  it('should display the covidVaccineName input when covidVaccinated is Yes', () => {
    component.form.get('covidVaccinated').setValue('Yes');
    fixture.detectChanges();
    const covidVaccineNameInput = fixture.nativeElement.querySelector('#covidVaccineName');
    expect(covidVaccineNameInput).toBeTruthy();
  });

  it('should display the covidVaccineNotes textarea when covidVaccinated is Yes or No', () => {
    component.form.get('covidVaccinated').setValue('Yes');
    fixture.detectChanges();
    let covidVaccineNotesTextarea = fixture.nativeElement.querySelector('#covidVaccineNotes');
    expect(covidVaccineNotesTextarea).toBeTruthy();

    component.form.get('covidVaccinated').setValue('No');
    fixture.detectChanges();
    covidVaccineNotesTextarea = fixture.nativeElement.querySelector('#covidVaccineNotes');
    expect(covidVaccineNotesTextarea).toBeTruthy();
  });
});
