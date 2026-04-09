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

import {CandidateCertificationFormComponent} from './candidate-certification-form.component';
import {CandidateCertification} from '../../../model/candidate-certification';
import {CandidateCertificationService} from '../../../services/candidate-certification.service';
import {CandidateService} from '../../../services/candidate.service';
import {RegistrationService} from '../../../services/registration.service';

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
  @Input() formControlName?: string;
  @Input() placeholder?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'app-date-picker',
  template: ''
})
class DatePickerStubComponent {
  @Input() id?: string;
  @Input() control?: unknown;
}

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() disabled?: boolean;
  @Output() onClick = new EventEmitter<void>();
}

function makeCertification(overrides: Partial<CandidateCertification> = {}): CandidateCertification {
  return {
    id: 1,
    name: 'AWS Certified',
    institution: 'Amazon',
    dateCompleted: '2024-01-15',
    ...overrides
  };
}

describe('CandidateCertificationFormComponent', () => {
  let component: CandidateCertificationFormComponent;
  let fixture: ComponentFixture<CandidateCertificationFormComponent>;

  let candidateCertificationServiceSpy: jasmine.SpyObj<CandidateCertificationService>;

  async function configureAndCreate(options?: {
    certificate?: CandidateCertification | null;
    createError?: unknown;
    updateError?: unknown;
  }) {
    candidateCertificationServiceSpy = jasmine.createSpyObj('CandidateCertificationService', [
      'createCandidateCertification',
      'update'
    ]);

    if (options?.createError) {
      candidateCertificationServiceSpy.createCandidateCertification.and.returnValue(throwError(options.createError));
    } else {
      candidateCertificationServiceSpy.createCandidateCertification.and.returnValue(of(makeCertification({id: 2})));
    }

    if (options?.updateError) {
      candidateCertificationServiceSpy.update.and.returnValue(throwError(options.updateError));
    } else {
      candidateCertificationServiceSpy.update.and.returnValue(of(makeCertification()));
    }

    await TestBed.configureTestingModule({
      declarations: [
        CandidateCertificationFormComponent,
        TcInputStubComponent,
        DatePickerStubComponent,
        TcButtonStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate'])},
        {provide: CandidateService, useValue: jasmine.createSpyObj('CandidateService', ['noop'])},
        {provide: CandidateCertificationService, useValue: candidateCertificationServiceSpy},
        {provide: RegistrationService, useValue: jasmine.createSpyObj('RegistrationService', ['next', 'back'])}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateCertificationFormComponent);
    component = fixture.componentInstance;
    component.certificate = options?.certificate ?? null;

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
      expect(component.form.contains('name')).toBeTrue();
      expect(component.form.contains('institution')).toBeTrue();
      expect(component.form.contains('dateCompleted')).toBeTrue();
    });

    it('should patch an existing certification into the form', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      await configureAndCreate({certificate: makeCertification()});

      expect(component.form.value.id).toBe(1);
      expect(component.form.value.name).toBe('AWS Certified');
      expect(component.form.value.institution).toBe('Amazon');
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-input controls for migrated fields', () => {
      const inputIds = fixture.debugElement.queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('name');
      expect(inputIds).toContain('institution');
    });

    it('should render tc-label elements and a tc-button', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;

      expect(nativeElement.querySelector('tc-label[for="name"]')).toBeTruthy();
      expect(nativeElement.querySelector('tc-label[for="dateCompleted"]')).toBeTruthy();
      expect(nativeElement.querySelectorAll('tc-button').length).toBe(1);
    });

    it('should render the migrated date field', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;

      expect(nativeElement.querySelector('tc-label[for="dateCompleted"]')).toBeTruthy();
      expect(component.form.contains('dateCompleted')).toBeTrue();
    });
  });

  describe('save', () => {
    beforeEach(async () => configureAndCreate());

    it('should create a certification when the form has no id', () => {
      const savedSpy = spyOn(component.saved, 'emit');
      component.form.patchValue({
        name: 'Azure Certified',
        institution: 'Microsoft',
        dateCompleted: '2024-01-15'
      });
      component.form.markAsDirty();

      component.save();

      expect(candidateCertificationServiceSpy.createCandidateCertification).toHaveBeenCalledWith(component.form.value);
      expect(savedSpy).toHaveBeenCalled();
    });

    it('should update a certification when the form has an id', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      await configureAndCreate({certificate: makeCertification()});
      component.form.patchValue({name: 'Azure Certified'});
      component.form.markAsDirty();

      component.save();

      expect(candidateCertificationServiceSpy.update).toHaveBeenCalledWith(component.form.value);
    });

    it('should emit the existing certification when the form is pristine', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const certificate = makeCertification();
      await configureAndCreate({certificate});
      const savedSpy = spyOn(component.saved, 'emit');

      component.save();

      expect(savedSpy).toHaveBeenCalledWith(certificate);
    });

    it('should set error and clear saving when create fails', async () => {
      // This test needs a different service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      await configureAndCreate({createError: serverError});
      component.form.patchValue({
        name: 'Azure Certified',
        institution: 'Microsoft',
        dateCompleted: '2024-01-15'
      });
      component.form.markAsDirty();

      component.save();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });

    it('should set error and clear saving when update fails', async () => {
      // This test needs a different input and service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 503};
      await configureAndCreate({certificate: makeCertification(), updateError: serverError});
      component.form.patchValue({name: 'Azure Certified'});
      component.form.markAsDirty();

      component.save();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });
  });
});
