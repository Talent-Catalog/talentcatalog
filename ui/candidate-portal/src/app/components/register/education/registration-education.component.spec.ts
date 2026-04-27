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

import {Component, EventEmitter, forwardRef, Input, NO_ERRORS_SCHEMA, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationEducationComponent} from './registration-education.component';
import {Country} from '../../../model/country';
import {EducationLevel} from '../../../model/education-level';
import {EducationMajor} from '../../../model/education-major';
import {CandidateEducation} from '../../../model/candidate-education';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateEducationService} from '../../../services/candidate-education.service';
import {CountryService} from '../../../services/country.service';
import {EducationLevelService} from '../../../services/education-level.service';
import {EducationMajorService} from '../../../services/education-major.service';
import {RegistrationService} from '../../../services/registration.service';

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

function makeEducationLevel(id: number, name: string, educationType: string): EducationLevel {
  return {
    id,
    name,
    level: id,
    educationType
  };
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
    ...overrides
  };
}

describe('RegistrationEducationComponent', () => {
  let component: RegistrationEducationComponent;
  let fixture: ComponentFixture<RegistrationEducationComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let candidateEducationServiceSpy: jasmine.SpyObj<CandidateEducationService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let educationLevelServiceSpy: jasmine.SpyObj<EducationLevelService>;
  let educationMajorServiceSpy: jasmine.SpyObj<EducationMajorService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;

  async function configureAndCreate(options?: {
    candidateEducations?: CandidateEducation[];
    countriesError?: unknown;
    levelsError?: unknown;
    majorsError?: unknown;
    candidateError?: unknown;
    saveError?: unknown;
    deleteError?: unknown;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', [
      'getCandidateEducation',
      'updateCandidateEducationLevel'
    ]);
    candidateEducationServiceSpy = jasmine.createSpyObj('CandidateEducationService', [
      'deleteCandidateEducation'
    ]);
    countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    educationLevelServiceSpy = jasmine.createSpyObj('EducationLevelService', ['listEducationLevels']);
    educationMajorServiceSpy = jasmine.createSpyObj('EducationMajorService', ['listMajors']);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);

    const candidateEducations = options?.candidateEducations ?? [makeEducation()];
    const countries = [makeCountry(1, 'Jordan'), makeCountry(2, 'Lebanon')];
    const educationLevels = [
      makeEducationLevel(1, 'Bachelor', 'Bachelor'),
      makeEducationLevel(2, 'Masters', 'Masters')
    ];
    const majors = [makeMajor(10, 'Engineering'), makeMajor(20, 'Mathematics')];

    if (options?.candidateError) {
      candidateServiceSpy.getCandidateEducation.and.returnValue(throwError(options.candidateError));
    } else {
      candidateServiceSpy.getCandidateEducation.and.returnValue(of({
        maxEducationLevel: educationLevels[0],
        candidateEducations
      } as any));
    }

    if (options?.saveError) {
      candidateServiceSpy.updateCandidateEducationLevel.and.returnValue(throwError(options.saveError));
    } else {
      candidateServiceSpy.updateCandidateEducationLevel.and.returnValue(of({} as any));
    }

    if (options?.deleteError) {
      candidateEducationServiceSpy.deleteCandidateEducation.and.returnValue(throwError(options.deleteError));
    } else {
      candidateEducationServiceSpy.deleteCandidateEducation.and.returnValue(of({} as any));
    }

    if (options?.countriesError) {
      countryServiceSpy.listCountries.and.returnValue(throwError(options.countriesError));
    } else {
      countryServiceSpy.listCountries.and.returnValue(of(countries));
    }

    if (options?.levelsError) {
      educationLevelServiceSpy.listEducationLevels.and.returnValue(throwError(options.levelsError));
    } else {
      educationLevelServiceSpy.listEducationLevels.and.returnValue(of(educationLevels));
    }

    if (options?.majorsError) {
      educationMajorServiceSpy.listMajors.and.returnValue(throwError(options.majorsError));
    } else {
      educationMajorServiceSpy.listMajors.and.returnValue(of(majors));
    }

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationEducationComponent,
        NgSelectStubComponent,
        TcButtonStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate'])},
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CandidateEducationService, useValue: candidateEducationServiceSpy},
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: EducationLevelService, useValue: educationLevelServiceSpy},
        {provide: EducationMajorService, useValue: educationMajorServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationEducationComponent);
    component = fixture.componentInstance;

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

    it('should load dropdown data and candidate education', () => {
      expect(countryServiceSpy.listCountries).toHaveBeenCalled();
      expect(educationLevelServiceSpy.listEducationLevels).toHaveBeenCalled();
      expect(educationMajorServiceSpy.listMajors).toHaveBeenCalled();
      expect(candidateServiceSpy.getCandidateEducation).toHaveBeenCalled();
      expect(component.candidateEducationItems.length).toBe(1);
      expect(component.form.value.maxEducationLevelId).toBe(1);
    });

    it('should build the expected form controls', () => {
      expect(component.form.contains('maxEducationLevelId')).toBeTrue();
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render the education level ng-select with the tc-select class', () => {
      const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));

      expect(selectEls.length).toBe(1);
      expect(selectEls[0].componentInstance.id).toBe('maxEducationLevelId');
      expect(selectEls[0].nativeElement.classList).toContain('tc-select');
    });

    it('should render tc-label for the migrated select field', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;

      expect(nativeElement.querySelector('tc-label[for="maxEducationLevelId"]')).toBeTruthy();
    });

    it('should render the add tc-button when not adding education', () => {
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(1);
    });

    it('should hide the add tc-button while adding education', () => {
      component.addingEducation = true;
      fixture.detectChanges();

      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(0);
    });
  });

  describe('behaviour', () => {
    beforeEach(async () => configureAndCreate());

    it('should set addingEducation when addEducation is called', () => {
      component.addEducation();

      expect(component.addingEducation).toBeTrue();
    });

    it('should delete a candidate education and clear saving on success', () => {
      component.deleteCandidateEducation(0);

      expect(candidateEducationServiceSpy.deleteCandidateEducation).toHaveBeenCalledWith(1);
      expect(component.candidateEducationItems.length).toBe(0);
      expect(component.saving).toBeFalse();
    });

    it('should set error and clear saving when delete fails', async () => {
      // This test needs a different service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      await configureAndCreate({deleteError: serverError});

      component.deleteCandidateEducation(0);

      expect(component.error).toEqual(serverError);
      expect(component.candidateEducationItems.length).toBe(1);
      expect(component.saving).toBeFalse();
    });
  });

  describe('save flows', () => {
    beforeEach(async () => configureAndCreate());

    it('should save and navigate next when next() is called', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.form.patchValue({maxEducationLevelId: 1});
      component.form.markAsDirty();
      component.next();

      expect(candidateServiceSpy.updateCandidateEducationLevel).toHaveBeenCalledWith(
        jasmine.objectContaining({maxEducationLevelId: 1})
      );
      expect(onSaveSpy).toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
      expect(component.saving).toBeFalse();
    });

    it('should save 0 when next() is called with a null education level', () => {
      component.form.patchValue({maxEducationLevelId: null});
      component.form.markAsDirty();

      component.next();

      expect(candidateServiceSpy.updateCandidateEducationLevel).toHaveBeenCalledWith(
        jasmine.objectContaining({maxEducationLevelId: 0})
      );
    });

    it('should save and navigate back when back() is called with a dirty valid form', () => {
      component.form.patchValue({maxEducationLevelId: 1});
      component.form.markAsDirty();

      component.back();

      expect(candidateServiceSpy.updateCandidateEducationLevel).toHaveBeenCalledWith(
        jasmine.objectContaining({maxEducationLevelId: 1})
      );
      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should go back without saving when the form is pristine', () => {
      component.back();

      expect(candidateServiceSpy.updateCandidateEducationLevel).not.toHaveBeenCalled();
      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should set error and not navigate when save fails on next()', async () => {
      // This test needs a different service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      await configureAndCreate({saveError: serverError});

      component.form.patchValue({maxEducationLevelId: 1});
      component.form.markAsDirty();
      component.next();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
      expect(registrationServiceSpy.next).not.toHaveBeenCalled();
    });
  });

  describe('cancel', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onSave when cancel is called', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.cancel();

      expect(onSaveSpy).toHaveBeenCalled();
    });
  });

  describe('error paths', () => {
    it('should set error when candidate education fails to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({candidateError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error when countries fail to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({countriesError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error when education levels fail to load', async () => {
      const serverError = {status: 502};
      await configureAndCreate({levelsError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error when majors fail to load', async () => {
      const serverError = {status: 504};
      await configureAndCreate({majorsError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });
});
