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
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {of} from 'rxjs';
import {LanguageThresholdComponent} from './language-threshold.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {LanguageService} from '../../../../../services/language.service';
import {Language} from '../../../../../model/language';
import {NgSelectModule} from '@ng-select/ng-select';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('LanguageThresholdComponent', () => {
  let component: LanguageThresholdComponent;
  let fixture: ComponentFixture<LanguageThresholdComponent>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const languageServiceSpy = jasmine.createSpyObj('LanguageService', ['listLanguages']);
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      declarations: [LanguageThresholdComponent, AutosaveStatusComponent],
      imports: [ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: LanguageService, useValue: languageServiceSpy },
        { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy }
      ]
    }).compileComponents();

    languageService = TestBed.inject(LanguageService) as jasmine.SpyObj<LanguageService>;
    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LanguageThresholdComponent);
    component = fixture.componentInstance;
    languageService.listLanguages.and.returnValue(of([
      { id: 1, name: 'English' } as Language,
      { id: 2, name: 'Spanish' } as Language
    ]));
    fixture.detectChanges(); // ngOnInit() is called here
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize languages with English first', () => {
    expect(component.languages.length).toBe(2);
    expect(component.languages[0].name).toBe('English');
  });

  it('should have a form with visaJobLanguagesRequired control', () => {
    expect(component.form.contains('visaJobLanguagesRequired')).toBeTrue();
  });

  it('should return true for hasRequiredLanguages if languages are selected', () => {
    component.form.patchValue({ visaJobLanguagesRequired: [1] });
    expect(component.hasRequiredLanguages).toBeTrue();
  });

  it('should return false for hasRequiredLanguages if no languages are selected', () => {
    component.form.patchValue({ visaJobLanguagesRequired: [] });
    expect(component.hasRequiredLanguages).toBeFalse();
  });

  it('should return true for hasNotes if visaJobLanguagesThresholdMet is Yes', () => {
    component.form.patchValue({ visaJobLanguagesThresholdMet: 'Yes' });
    expect(component.hasNotes).toBeTrue();
  });

  it('should return true for hasNotes if visaJobLanguagesThresholdMet is No', () => {
    component.form.patchValue({ visaJobLanguagesThresholdMet: 'No' });
    expect(component.hasNotes).toBeTrue();
  });

  it('should return false for hasNotes if visaJobLanguagesThresholdMet is not set', () => {
    component.form.patchValue({ visaJobLanguagesThresholdMet: null });
    expect(component.hasNotes).toBeFalse();
  });
});
