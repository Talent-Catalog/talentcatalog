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
import {DependantsComponent} from "./dependants.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateIntakeData} from "../../../../model/candidate";
import {Country} from "../../../../model/country";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateDependantService} from "../../../../services/candidate-dependant.service";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('DependantsComponent', () => {
  let component: DependantsComponent;
  let fixture: ComponentFixture<DependantsComponent>;

  const mockCandidateIntakeData: CandidateIntakeData = {
    candidateDependants: [
      // Mock dependants data
    ]
  };

  const mockNationalities: Country[] = [
    // Mock nationalities data
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      declarations: [DependantsComponent, AutosaveStatusComponent],
      providers: [CandidateDependantService],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DependantsComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    component.candidateIntakeData = mockCandidateIntakeData;
    component.nationalities = mockNationalities;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with provided candidate data', () => {
    expect(component.error).toBeFalsy();
    expect(component.candidate).toEqual(new MockCandidate());
    expect(component.candidateIntakeData).toEqual(mockCandidateIntakeData);
  });

  it('should display no dependants message if candidateDependants array is empty', () => {
    // Set candidateDependants array to empty
    component.candidateIntakeData.candidateDependants = [];
    fixture.detectChanges();

    const noDependantsMessage = fixture.nativeElement.querySelector('.mb-0');
    expect(noDependantsMessage.textContent.trim()).toBe('No Dependants Added');
  });

  it('should render dependant cards for each dependant in candidateDependants array', () => {
    const dependantCards = fixture.nativeElement.querySelectorAll('.dependant-card');
    expect(dependantCards.length).toBe(mockCandidateIntakeData.candidateDependants.length);
  });
});
