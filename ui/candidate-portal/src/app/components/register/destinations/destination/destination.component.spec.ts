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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DestinationComponent} from './destination.component';
import {ReactiveFormsModule} from '@angular/forms';
import {RegistrationService} from '../../../../services/registration.service';
import {CandidateDestination, YesNoUnsureLearn} from '../../../../model/candidate';
import {NgSelectModule} from '@ng-select/ng-select';
import {TranslateModule} from '@ngx-translate/core';
import {Country} from "../../../../model/country";

describe('DestinationComponent', () => {
  let component: DestinationComponent;
  let fixture: ComponentFixture<DestinationComponent>;

  const mockCandidateDestination: CandidateDestination = {
    id: 1,
    interest: YesNoUnsureLearn.Yes,
    notes: 'Looking forward to this opportunity.'
  };

  const mockCountry: Country = {
    id: 101,
    name: 'Canada',
    isoCode: 'CA',
    status: 'active',
    translatedName: null
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DestinationComponent],
      imports: [
        ReactiveFormsModule,
        NgSelectModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {provide: RegistrationService, useValue: {}}
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationComponent);
    component = fixture.componentInstance;
    component.candidateDestination = mockCandidateDestination;
    component.country = mockCountry;
    component.saving = false;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidateDestination and country data', () => {
    const formValue = component.form.value;
    expect(formValue.id).toEqual(mockCandidateDestination.id);
    expect(formValue.countryId).toEqual(mockCountry.id);
    expect(formValue.interest).toEqual(mockCandidateDestination.interest);
    expect(formValue.notes).toEqual(mockCandidateDestination.notes);
  });

  it('should return correct interest value from getter', () => {
    expect(component.interest).toEqual(mockCandidateDestination.interest);
  });

  it('should mark interest as invalid when not provided', () => {
    component.form.controls['interest'].setValue(null);
    expect(component.form.controls['interest'].valid).toBeFalse();
  });

  it('should mark interest as valid when provided', () => {
    component.form.controls['interest'].setValue(YesNoUnsureLearn.No);
    expect(component.form.controls['interest'].valid).toBeTrue();
  });
});
