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

import {MaritalStatusComponent} from "./marital-status.component";
import {CandidateService} from "../../../../services/candidate.service";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('MaritalStatusComponent', () => {
  let component: MaritalStatusComponent;
  let fixture: ComponentFixture<MaritalStatusComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MaritalStatusComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaritalStatusComponent);
    component = fixture.componentInstance;
    component.educationLevels = [
      { id: 1, name: 'High School', status: 'Active', level: 12 },
      { id: 2, name: 'Bachelor', status: 'Active', level: 16 }
    ];
    component.occupations = [
      { id: 1, name: 'Engineer', isco08Code: '1234', status: 'Active' },
      { id: 2, name: 'Teacher', isco08Code: '5678', status: 'Active' }
    ];
    component.languageLevels = [
      { id: 1, name: 'Basic', level: 1, status: 'Active' },
      { id: 2, name: 'Intermediate', level: 2, status: 'Active' }
    ];
    component.nationalities = [
      { id: 1, name: 'USA', status: 'Active', translatedName: 'United States' },
      { id: 2, name: 'Canada', status: 'Active', translatedName: 'Canada' }
    ];
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with default values', () => {
    expect(component.form.value).toEqual({
      maritalStatus: null,
      maritalStatusNotes: null,
      partnerRegistered: null,
      partnerCandId: null,
      partnerEduLevelId: null,
      partnerEduLevelNotes: null,
      partnerOccupationId: null,
      partnerOccupationNotes: null,
      partnerEnglish: null,
      partnerEnglishLevelId: null,
      partnerIelts: null,
      partnerIeltsScore: null,
      partnerIeltsYr: null,
      partnerCitizenship: null,
    });
  });

  it('should remain valid when optional fields are empty', () => {
    // Initialize the form without providing values for optional fields
    component.ngOnInit();

    // Assert that the form is valid
    expect(component.form.valid).toBeTruthy();
  });

  it('should validate IELTS score', () => {
    component.form.patchValue({
      partnerIelts: 'YesGeneral', // IELTS score required for this option
      partnerIeltsScore: 6.5, // Valid IELTS score
    });
    expect(component.form.valid).toBeTruthy();
  });

});
