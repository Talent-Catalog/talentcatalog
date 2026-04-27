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

import {Component, forwardRef, Input, NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {CandidateJobExperienceFormComponent} from './candidate-job-experience-form.component';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {Country} from '../../../model/country';
import {Occupation} from '../../../model/occupation';
import {CountryService} from '../../../services/country.service';
import {CandidateOccupationService} from '../../../services/candidate-occupation.service';
import {CandidateJobExperienceService} from '../../../services/candidate-job-experience.service';

@Component({
  selector: 'tc-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcInputStubComponent),
    multi: true
  }]
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() type?: string;
  @Input() min?: number;
  @Input() formControlName?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'ng-select',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => NgSelectStubComponent),
    multi: true
  }]
})
class NgSelectStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() items?: unknown[];
  @Input() clearable?: boolean;
  @Input() bindValue?: string;
  @Input() bindLabel?: string;
  @Input() placeholder?: string;
  @Input() formControlName?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'tc-date-picker',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcDatePickerStubComponent),
    multi: true
  }]
})
class TcDatePickerStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() control?: unknown;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'tc-radio',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcRadioStubComponent),
    multi: true
  }]
})
class TcRadioStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() name?: string;
  @Input() value?: unknown;
  @Input() formControlName?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'ngx-wig',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => NgxWigStubComponent),
    multi: true
  }]
})
class NgxWigStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() formControlName?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

function makeCountry(id: number, name: string): Country {
  return {id, name, status: 'active', translatedName: name};
}

function makeOccupation(id: number, name: string): Occupation {
  return {id, name};
}

function makeCandidateOccupation(id: number, occupationId: number, name: string): CandidateOccupation {
  return {
    id,
    occupation: makeOccupation(occupationId, name),
    occupationId,
    yearsExperience: 5
  };
}

function makeExperience(overrides: Partial<CandidateJobExperience> = {}): CandidateJobExperience {
  const country = makeCountry(1, 'Jordan');
  const candidateOccupation = makeCandidateOccupation(10, 100, 'Engineer');

  return {
    id: 1,
    companyName: 'ACME',
    role: 'Engineer',
    startDate: '2020-01-01',
    endDate: '2021-01-01',
    fullTime: true as any,
    paid: true as any,
    description: 'desc',
    country,
    countryId: country.id,
    candidateOccupation,
    candidateOccupationId: candidateOccupation.id,
    ...overrides
  };
}

describe('CandidateJobExperienceFormComponent', () => {
  let component: CandidateJobExperienceFormComponent;
  let fixture: ComponentFixture<CandidateJobExperienceFormComponent>;
  let scrollIntoViewSpyInstalled = false;

  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let candidateOccupationServiceSpy: jasmine.SpyObj<CandidateOccupationService>;
  let jobExperienceServiceSpy: jasmine.SpyObj<CandidateJobExperienceService>;

  async function configureAndCreate(options?: {
    countries?: Country[] | null;
    candidateOccupations?: CandidateOccupation[];
    candidateOccupation?: CandidateOccupation | null;
    candidateJobExperience?: CandidateJobExperience | null;
    countriesError?: unknown;
    occupationsError?: unknown;
  }) {
    countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    candidateOccupationServiceSpy = jasmine.createSpyObj('CandidateOccupationService', ['listMyOccupations']);
    jobExperienceServiceSpy = jasmine.createSpyObj('CandidateJobExperienceService', ['createJobExperience', 'updateJobExperience']);

    const countries = options?.countries ?? [makeCountry(1, 'Jordan'), makeCountry(2, 'Lebanon')];
    const candidateOccupations = options?.candidateOccupations ?? [
      makeCandidateOccupation(10, 100, 'Engineer'),
      makeCandidateOccupation(20, 200, 'Teacher')
    ];

    if (options?.countriesError) {
      countryServiceSpy.listCountries.and.returnValue(throwError(options.countriesError));
    } else {
      countryServiceSpy.listCountries.and.returnValue(of(countries));
    }

    if (options?.occupationsError) {
      candidateOccupationServiceSpy.listMyOccupations.and.returnValue(throwError(options.occupationsError));
    } else {
      candidateOccupationServiceSpy.listMyOccupations.and.returnValue(of(candidateOccupations));
    }

    jobExperienceServiceSpy.createJobExperience.and.returnValue(of(makeExperience({id: 2})));
    jobExperienceServiceSpy.updateJobExperience.and.returnValue(of(makeExperience()));

    await TestBed.configureTestingModule({
      declarations: [
        CandidateJobExperienceFormComponent,
        TcInputStubComponent,
        NgSelectStubComponent,
        TcDatePickerStubComponent,
        TcRadioStubComponent,
        NgxWigStubComponent
      ],
      imports: [FormsModule, ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: CandidateOccupationService, useValue: candidateOccupationServiceSpy},
        {provide: CandidateJobExperienceService, useValue: jobExperienceServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateJobExperienceFormComponent);
    component = fixture.componentInstance;
    component.countries = options?.countries === null ? null : countries;
    component.candidateOccupations = candidateOccupations;
    component.candidateOccupation = options?.candidateOccupation ?? null;
    component.candidateJobExperience = options?.candidateJobExperience ?? null;

    const translateService = TestBed.inject(TranslateService);
    translateService.use('en');

    if (!scrollIntoViewSpyInstalled) {
      spyOn(HTMLElement.prototype, 'scrollIntoView').and.stub();
      scrollIntoViewSpyInstalled = true;
    }
    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    beforeEach(async () => configureAndCreate());

    it('should build the expected form controls', () => {
      expect(component.form.contains('id')).toBeTrue();
      expect(component.form.contains('companyName')).toBeTrue();
      expect(component.form.contains('country')).toBeTrue();
      expect(component.form.contains('candidateOccupationId')).toBeTrue();
      expect(component.form.contains('role')).toBeTrue();
      expect(component.form.contains('startDate')).toBeTrue();
      expect(component.form.contains('endDate')).toBeTrue();
      expect(component.form.contains('fullTime')).toBeTrue();
      expect(component.form.contains('paid')).toBeTrue();
      expect(component.form.contains('description')).toBeTrue();
    });

    it('should assign separate radio names for contract and employment types', () => {
      const radios = fixture.debugElement.queryAll(By.directive(TcRadioStubComponent))
        .map(debugEl => debugEl.componentInstance as TcRadioStubComponent);

      const radioNamesById = Object.fromEntries(radios.map(radio => [radio.id, radio.name]));

      expect(radioNamesById['fullTime']).toBe('contractType');
      expect(radioNamesById['partTime']).toBe('contractType');
      expect(radioNamesById['paid']).toBe('employmentType');
      expect(radioNamesById['voluntary']).toBe('employmentType');
    });

    it('should load candidate occupations on init', () => {
      expect(candidateOccupationServiceSpy.listMyOccupations).toHaveBeenCalled();
      expect(component.candidateOccupations.length).toBe(2);
    });

    it('should not load countries when countries input is already provided', () => {
      expect(countryServiceSpy.listCountries).not.toHaveBeenCalled();
      expect(component.loading).toBeFalse();
    });

    it('should load countries when countries input is missing', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      await configureAndCreate({countries: null});

      expect(countryServiceSpy.listCountries).toHaveBeenCalled();
      expect(component.countries.length).toBe(2);
      expect(component.loading).toBeFalse();
    });

    it('should patch existing job experience values into the form', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const experience = makeExperience();
      await configureAndCreate({candidateJobExperience: experience});

      expect(component.form.value.id).toBe(1);
      expect(component.form.value.companyName).toBe('ACME');
      expect(component.form.value.country).toBe(1);
      expect(component.form.value.candidateOccupationId).toBe(10);
      expect(component.form.value.role).toBe('Engineer');
    });

    it('should patch candidateOccupationId from candidateOccupation when creating a new experience', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const occupation = makeCandidateOccupation(20, 200, 'Teacher');
      await configureAndCreate({candidateOccupation: occupation});

      expect(component.form.value.candidateOccupationId).toBe(20);
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-input controls for companyName and role', () => {
      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('companyName');
      expect(inputIds).toContain('role');
    });

    it('should render ng-select controls with the tc-select class', () => {
      const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
      const selectIds = selectEls.map(debugEl => debugEl.componentInstance.id);

      expect(selectIds).toContain('candidateOccupationId');
      expect(selectIds).toContain('country');
      selectEls.forEach(debugEl => {
        expect(debugEl.nativeElement.classList).toContain('tc-select');
      });
    });

    it('should render tc-date-picker and tc-radio controls', () => {
      expect(fixture.debugElement.queryAll(By.directive(TcDatePickerStubComponent)).length).toBe(2);
      expect(fixture.debugElement.queryAll(By.directive(TcRadioStubComponent)).length).toBe(4);
    });
  });

  describe('validation', () => {
    beforeEach(async () => configureAndCreate());

    it('should be invalid when required fields are empty', () => {
      component.form.patchValue({
        companyName: '',
        country: null,
        candidateOccupationId: null,
        role: '',
        startDate: null,
        fullTime: null,
        paid: null,
        description: ''
      });

      expect(component.form.invalid).toBeTrue();
    });

    it('should set invalidDate when startDate is after endDate', () => {
      // The form-group validator re-runs when patchValue updates either date control.
      component.form.patchValue({
        startDate: '2022-01-01',
        endDate: '2021-01-01'
      });

      expect(component.form.hasError('invalidDate')).toBeTrue();
    });
  });

  describe('save', () => {
    beforeEach(async () => configureAndCreate());

    it('should create a job experience when the form has no id', () => {
      component.form.patchValue({
        companyName: 'ACME',
        country: 1,
        candidateOccupationId: 10,
        role: 'Engineer',
        startDate: '2020-01-01',
        endDate: '2021-01-01',
        fullTime: true,
        paid: true,
        description: 'desc'
      });

      component.save();

      expect(jobExperienceServiceSpy.createJobExperience).toHaveBeenCalledWith(component.form.value);
    });

    it('should attach candidateOccupation and emit formSaved on create success', () => {
      const formSavedSpy = spyOn(component.formSaved, 'emit');
      component.candidateOccupation = makeCandidateOccupation(10, 100, 'Engineer');
      component.form.patchValue({
        companyName: 'ACME',
        country: 1,
        candidateOccupationId: 10,
        role: 'Engineer',
        startDate: '2020-01-01',
        endDate: '2021-01-01',
        fullTime: true,
        paid: true,
        description: 'desc'
      });

      component.save();

      expect(formSavedSpy).toHaveBeenCalled();
      expect(component.saving).toBeFalse();
    });

    it('should update a job experience when the form has an id', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const experience = makeExperience();
      await configureAndCreate({candidateJobExperience: experience});

      component.save();

      expect(jobExperienceServiceSpy.updateJobExperience).toHaveBeenCalledWith(component.form.value);
    });

    it('should set error and clear saving when create fails', () => {
      const serverError = {status: 500};
      jobExperienceServiceSpy.createJobExperience.and.returnValue(throwError(serverError));
      component.form.patchValue({
        companyName: 'ACME',
        country: 1,
        candidateOccupationId: 10,
        role: 'Engineer',
        startDate: '2020-01-01',
        endDate: '2021-01-01',
        fullTime: true,
        paid: true,
        description: 'desc'
      });

      component.save();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });

    it('should set error and clear saving when update fails', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      const experience = makeExperience();
      await configureAndCreate({candidateJobExperience: experience});
      jobExperienceServiceSpy.updateJobExperience.and.returnValue(throwError(serverError));

      component.save();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });
  });

  describe('emitSaveEvent', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit formSaved and clear saving', () => {
      const formSavedSpy = spyOn(component.formSaved, 'emit');
      component.saving = true;
      const experience = makeExperience();

      component.emitSaveEvent(experience);

      expect(formSavedSpy).toHaveBeenCalledWith(experience);
      expect(component.saving).toBeFalse();
    });
  });

  describe('cancel', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit formClosed', () => {
      const formClosedSpy = spyOn(component.formClosed, 'emit');

      component.cancel();

      expect(formClosedSpy).toHaveBeenCalled();
    });
  });

  describe('error paths', () => {
    it('should set error and clear loading when countries fail to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({countries: null, countriesError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error when candidate occupations fail to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({occupationsError: serverError});

      expect(component.error).toEqual(serverError);
    });
  });
});
