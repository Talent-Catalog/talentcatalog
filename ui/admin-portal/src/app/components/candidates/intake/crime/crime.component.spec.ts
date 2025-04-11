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
import {CrimeComponent} from "./crime.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {YesNoUnsure} from "../../../../model/candidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('CrimeComponent', () => {
  let component: CrimeComponent;
  let fixture: ComponentFixture<CrimeComponent>;

  const mockCandidateService = {
    candidateIntakeData: {
      crimeConvict: YesNoUnsure.Yes, // Mocking crime convict data
      crimeConvictNotes: 'Test crime convict notes' // Mocking crime convict notes data
    }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CrimeComponent, AutosaveStatusComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService, useValue: mockCandidateService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CrimeComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = mockCandidateService.candidateIntakeData;
    // Manually trigger ngOnInit
    component.ngOnInit();

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate data', () => {
    // Access the form control values
    const crimeConvictControl = component.form.get('crimeConvict');
    const crimeConvictNotesControl = component.form.get('crimeConvictNotes');

    // Check if form controls are initialized with the correct values
    expect(crimeConvictControl.value).toEqual(mockCandidateService.candidateIntakeData.crimeConvict);
    expect(crimeConvictNotesControl.value).toEqual(mockCandidateService.candidateIntakeData.crimeConvictNotes);
  });
  it('should show crime notes if crime is "Yes" or "Unsure"', () => {
    // Set crimeConvict to "Yes"
    component.form.get('crimeConvict').setValue(YesNoUnsure.Yes);
    expect(component.hasNotes).toBeTrue();

    // Set crimeConvict to "Unsure"
    component.form.get('crimeConvict').setValue(YesNoUnsure.Unsure);
    expect(component.hasNotes).toBeTrue();

    // Set crimeConvict to "No"
    component.form.get('crimeConvict').setValue(YesNoUnsure.No);
    expect(component.hasNotes).toBeFalse();
  });
});
