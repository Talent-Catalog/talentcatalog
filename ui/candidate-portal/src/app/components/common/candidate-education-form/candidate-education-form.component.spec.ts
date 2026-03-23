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

import {Component, EventEmitter, forwardRef, Input, Output, NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {CandidateEducationFormComponent} from './candidate-education-form.component';
import {CandidateEducation} from '../../../model/candidate-education';
import {Country} from '../../../model/country';
import {EducationMajor} from '../../../model/education-major';
import {CandidateEducationService} from '../../../services/candidate-education.service';
import {CountryService} from '../../../services/country.service';
import {EducationMajorService} from '../../../services/education-major.service';

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
  @Input() placeholder?: string;
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
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() type?: string;
  @Input() disabled?: boolean;
  @Output() onClick = new EventEmitter<void>();
}

function makeCountry(id: number, name: string): Country {
  return {
    id,
    name,
    status: 'active',
    translatedName: name
  };
}

function makeMajor(id: number, name: string): EducationMajor {
  return {id, name};
}

function makeEducation(overrides: Partial<CandidateEducation> = {}): CandidateEducation {
  return {
    id: 1,
    educationType: 'Bachelor',
    courseName: 'Computer Science',
    institution: 'Example University',
    lengthOfCourseYears: 4,
    yearCompleted: '2024',
    country: makeCountry(1, 'Jordan'),
    educationMajor: makeMajor(10, 'Engineering'),
    incomplete: false,
    ...overrides
  };
}

describe('CandidateEducationFormComponent', () => {
  let component: CandidateEducationFormComponent;
  let fixture: ComponentFixture<CandidateEducationFormComponent>;

  let candidateEducationServiceSpy: jasmine.SpyObj<CandidateEducationService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let majorServiceSpy: jasmine.SpyObj<EducationMajorService>;

  async function configureAndCreate(options?: {
    candidateEducation?: CandidateEducation | null;
    countries?: Country[] | null;
    majors?: EducationMajor[] | null;
    countriesError?: unknown;
    majorsError?: unknown;
  }) {
    candidateEducationServiceSpy = jasmine.createSpyObj('CandidateEducationService', [
      'createCandidateEducation',
      'updateCandidateEducation'
    ]);
    countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    majorServiceSpy = jasmine.createSpyObj('EducationMajorService', ['listMajors']);

    const countries = options?.countries ?? [makeCountry(1, 'Jordan'), makeCountry(2, 'Lebanon')];
    const majors = options?.majors ?? [makeMajor(10, 'Engineering'), makeMajor(20, 'Mathematics')];

    candidateEducationServiceSpy.createCandidateEducation.and.returnValue(of(makeEducation({id: 2})));
    candidateEducationServiceSpy.updateCandidateEducation.and.returnValue(of(makeEducation()));

    if (options?.countriesError) {
      countryServiceSpy.listCountries.and.returnValue(throwError(options.countriesError));
    } else {
      countryServiceSpy.listCountries.and.returnValue(of(countries));
    }

    if (options?.majorsError) {
      majorServiceSpy.listMajors.and.returnValue(throwError(options.majorsError));
    } else {
      majorServiceSpy.listMajors.and.returnValue(of(majors));
    }

    await TestBed.configureTestingModule({
      declarations: [
        CandidateEducationFormComponent,
        TcInputStubComponent,
        NgSelectStubComponent,
        TcButtonStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate'])},
        {provide: CandidateEducationService, useValue: candidateEducationServiceSpy},
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: EducationMajorService, useValue: majorServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateEducationFormComponent);
    component = fixture.componentInstance;
    component.educationType = 'Bachelor';
    component.candidateEducation = options?.candidateEducation ?? null;
    component.countries = options?.countries === null ? null : countries;
    component.majors = options?.majors === null ? null : majors;

    const translateService = TestBed.inject(TranslateService);
    translateService.use('en');

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
      expect(component.form.contains('educationType')).toBeTrue();
      expect(component.form.contains('educationMajorId')).toBeTrue();
      expect(component.form.contains('courseName')).toBeTrue();
      expect(component.form.contains('countryId')).toBeTrue();
      expect(component.form.contains('institution')).toBeTrue();
      expect(component.form.contains('lengthOfCourseYears')).toBeTrue();
      expect(component.form.contains('yearCompleted')).toBeTrue();
      expect(component.form.contains('incomplete')).toBeTrue();
    });

    it('should load countries and majors when inputs are missing', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      await configureAndCreate({countries: null, majors: null});

      expect(countryServiceSpy.listCountries).toHaveBeenCalled();
      expect(majorServiceSpy.listMajors).toHaveBeenCalled();
      expect(component.countries.length).toBe(2);
      expect(component.majors.length).toBe(2);
    });

    it('should patch existing education into the form', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const education = makeEducation();
      await configureAndCreate({candidateEducation: education});

      expect(component.form.value.id).toBe(1);
      expect(component.form.value.educationType).toBe('Bachelor');
      expect(component.form.value.courseName).toBe('Computer Science');
      expect(component.form.value.countryId).toBe(1);
      expect(component.form.value.educationMajorId).toBe(10);
    });

    it('should default educationType from the component input for new education', () => {
      expect(component.form.value.educationType).toBe('Bachelor');
      expect(component.form.value.id).toBeNull();
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-input controls for migrated text fields', () => {
      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('courseName');
      expect(inputIds).toContain('institution');
      expect(inputIds).toContain('lengthOfCourseYears');
    });

    it('should render ng-select controls with the tc-select class', () => {
      const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
      const selectIds = selectEls.map(debugEl => debugEl.componentInstance.id);

      expect(selectIds).toContain('educationType');
      expect(selectIds).toContain('educationMajorId');
      expect(selectIds).toContain('countryId');
      expect(selectIds).toContain('yearCompleted');
      selectEls.forEach(debugEl => {
        expect(debugEl.nativeElement.classList).toContain('tc-select');
      });
    });

    it('should render tc-label elements for migrated fields', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;

      expect(nativeElement.querySelector('tc-label[for="educationType"]')).toBeTruthy();
      expect(nativeElement.querySelector('tc-label[for="courseName"]')).toBeTruthy();
      expect(nativeElement.querySelector('tc-label[for="countryId"]')).toBeTruthy();
    });

    it('should render tc-button actions', () => {
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(2);
    });
  });

  describe('save', () => {
    beforeEach(async () => configureAndCreate());

    it('should create education when the form has no id', () => {
      const savedSpy = spyOn(component.saved, 'emit');

      component.form.patchValue({courseName: 'Updated'});
      component.form.markAsDirty();

      component.save();

      expect(candidateEducationServiceSpy.createCandidateEducation).toHaveBeenCalledWith(component.form.value);
      expect(savedSpy).toHaveBeenCalled();
    });

    it('should update education when the form has an id', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      await configureAndCreate({candidateEducation: makeEducation()});

      component.form.patchValue({courseName: 'Updated'});
      component.form.markAsDirty();
      component.save();

      expect(candidateEducationServiceSpy.updateCandidateEducation).toHaveBeenCalledWith(
        jasmine.objectContaining({
          id: 1,
          educationMajorId: 10,
          majorId: 10
        })
      );
    });

    it('should set error and clear saving when create fails', () => {
      const serverError = {status: 500};
      candidateEducationServiceSpy.createCandidateEducation.and.returnValue(throwError(serverError));

      component.form.patchValue({courseName: 'Updated'});
      component.form.markAsDirty();
      component.save();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });

    it('should set error and clear saving when update fails', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 503};
      await configureAndCreate({candidateEducation: makeEducation()});
      candidateEducationServiceSpy.updateCandidateEducation.and.returnValue(throwError(serverError));

      component.form.patchValue({courseName: 'Updated'});
      component.form.markAsDirty();
      component.save();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });

    it('should emit closed when cancel is called', () => {
      const closedSpy = spyOn(component.closed, 'emit');

      component.cancel();

      expect(closedSpy).toHaveBeenCalled();
    });
  });

  describe('error paths', () => {
    it('should set error when countries fail to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({countries: null, countriesError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error when majors fail to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({majors: null, majorsError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });
});
