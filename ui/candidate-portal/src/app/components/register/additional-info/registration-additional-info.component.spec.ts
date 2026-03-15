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

import {Component, EventEmitter, forwardRef, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationAdditionalInfoComponent} from './registration-additional-info.component';
import {CandidateService} from '../../../services/candidate.service';
import {RegistrationService} from '../../../services/registration.service';
import {SurveyTypeService} from '../../../services/survey-type.service';
import {SurveyType, US_AFGHAN_SURVEY_TYPE} from '../../../model/survey-type';

@Component({
  selector: 'tc-loading',
  template: ''
})
class TcLoadingStubComponent {
  @Input() loading?: boolean;
}

@Component({
  selector: 'app-error',
  template: ''
})
class ErrorStubComponent {
  @Input() error?: unknown;
}

@Component({
  selector: 'app-registration-footer',
  template: ''
})
class RegistrationFooterStubComponent {
  @Input() nextDisabled?: boolean;
  @Input() type?: string;
  @Output() backClicked = new EventEmitter<void>();
  @Output() nextClicked = new EventEmitter<void>();
}

@Component({
  selector: 'tc-label',
  template: '<ng-content></ng-content>'
})
class TcLabelStubComponent {
  @Input() for?: string;
}

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
  selector: 'tc-textarea',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcTextareaStubComponent),
    multi: true
  }]
})
class TcTextareaStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() rows?: string;
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
  @Input() bindLabel?: string;
  @Input() bindValue?: string;
  @Input() placeholder?: string;
  @Input() formControlName?: string;

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

function makeSurveyType(id: number, name: string): SurveyType {
  return {id, name} as SurveyType;
}

describe('RegistrationAdditionalInfoComponent', () => {
  let component: RegistrationAdditionalInfoComponent;
  let fixture: ComponentFixture<RegistrationAdditionalInfoComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;
  let surveyTypeServiceSpy: jasmine.SpyObj<SurveyTypeService>;

  async function configureAndCreate(options?: {
    surveyResponse?: any;
    additionalInfoResponse?: any;
    surveyTypes?: SurveyType[];
    candidateSurveyError?: unknown;
    additionalInfoError?: unknown;
    surveyTypesError?: unknown;
    updateSurveyError?: unknown;
    updateOtherInfoError?: unknown;
    edit?: boolean;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', [
      'getCandidateSurvey',
      'getCandidateAdditionalInfo',
      'updateCandidateSurvey',
      'updateCandidateOtherInfo'
    ]);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);
    surveyTypeServiceSpy = jasmine.createSpyObj('SurveyTypeService', ['listActiveSurveyTypes']);

    const surveyTypes = options?.surveyTypes ?? [
      makeSurveyType(1, 'Friend'),
      makeSurveyType(8, 'Other'),
      makeSurveyType(2, 'Agency')
    ];
    const surveyResponse = options?.surveyResponse ?? {
      surveyType: {id: 2},
      surveyComment: 'From an agency'
    };
    const additionalInfoResponse = options?.additionalInfoResponse ?? {
      additionalInfo: 'More context',
      linkedInLink: 'https://www.linkedin.com/in/example',
      allNotifications: false
    };

    if (options?.candidateSurveyError) {
      candidateServiceSpy.getCandidateSurvey.and.returnValue(throwError(options.candidateSurveyError));
    } else {
      candidateServiceSpy.getCandidateSurvey.and.returnValue(of(surveyResponse));
    }

    if (options?.additionalInfoError) {
      candidateServiceSpy.getCandidateAdditionalInfo.and.returnValue(throwError(options.additionalInfoError));
    } else {
      candidateServiceSpy.getCandidateAdditionalInfo.and.returnValue(of(additionalInfoResponse));
    }

    if (options?.surveyTypesError) {
      surveyTypeServiceSpy.listActiveSurveyTypes.and.returnValue(throwError(options.surveyTypesError));
    } else {
      surveyTypeServiceSpy.listActiveSurveyTypes.and.returnValue(of(surveyTypes));
    }

    if (options?.updateSurveyError) {
      candidateServiceSpy.updateCandidateSurvey.and.returnValue(throwError(options.updateSurveyError));
    } else {
      candidateServiceSpy.updateCandidateSurvey.and.returnValue(of({} as any));
    }

    if (options?.updateOtherInfoError) {
      candidateServiceSpy.updateCandidateOtherInfo.and.returnValue(throwError(options.updateOtherInfoError));
    } else {
      candidateServiceSpy.updateCandidateOtherInfo.and.returnValue(of({} as any));
    }

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationAdditionalInfoComponent,
        TcLoadingStubComponent,
        ErrorStubComponent,
        RegistrationFooterStubComponent,
        TcLabelStubComponent,
        TcInputStubComponent,
        TcTextareaStubComponent,
        NgSelectStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate'])},
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy},
        {provide: SurveyTypeService, useValue: surveyTypeServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationAdditionalInfoComponent);
    component = fixture.componentInstance;
    component.edit = options?.edit ?? false;

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
      expect(component.form.contains('additionalInfo')).toBeTrue();
      expect(component.form.contains('surveyTypeId')).toBeTrue();
      expect(component.form.contains('surveyComment')).toBeTrue();
      expect(component.form.contains('linkedInLink')).toBeTrue();
      expect(component.form.contains('allNotifications')).toBeTrue();
    });

    it('should load survey types and candidate additional info for non-US-Afghan flow', () => {
      expect(candidateServiceSpy.getCandidateSurvey).toHaveBeenCalled();
      expect(candidateServiceSpy.getCandidateAdditionalInfo).toHaveBeenCalled();
      expect(surveyTypeServiceSpy.listActiveSurveyTypes).toHaveBeenCalled();
      expect(component.usAfghan).toBeFalsy();
      expect(component.form.value.surveyTypeId).toBe(2);
      expect(component.form.value.surveyComment).toBe('From an agency');
      expect(component.form.value.additionalInfo).toBe('More context');
      expect(component.form.value.linkedInLink).toBe('https://www.linkedin.com/in/example');
      expect(component.loading).toBeFalse();
    });

    it('should remove survey controls for the US-Afghan flow', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({
        surveyResponse: {
          surveyType: {id: US_AFGHAN_SURVEY_TYPE}
        }
      });

      expect(component.usAfghan).toBeTrue();
      expect(component.form.contains('surveyTypeId')).toBeFalse();
      expect(component.form.contains('surveyComment')).toBeFalse();
      expect(surveyTypeServiceSpy.listActiveSurveyTypes).not.toHaveBeenCalled();
      expect(component.loading).toBeFalse();
    });

    it('should sort survey types with Other last', () => {
      expect(component.surveyTypes.map(surveyType => surveyType.id)).toEqual([1, 2, 8]);
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-loading while using the migrated loading component', () => {
      const loadingEl = fixture.debugElement.query(By.directive(TcLoadingStubComponent));

      expect(loadingEl).toBeTruthy();
      expect(loadingEl.componentInstance.loading).toBe(component.loading);
    });

    it('should render tc-label elements for the migrated fields', () => {
      const labelFors = fixture.debugElement
        .queryAll(By.directive(TcLabelStubComponent))
        .map(debugEl => debugEl.componentInstance.for);

      expect(labelFors).toContain('additionalInfo');
      expect(labelFors).toContain('surveyTypeId');
      expect(labelFors).toContain('surveyComment');
      expect(labelFors).toContain('linkedInLink');
    });

    it('should render the migrated textarea, inputs, and select', () => {
      const textareaEls = fixture.debugElement.queryAll(By.directive(TcTextareaStubComponent));
      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);
      const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));

      expect(textareaEls.length).toBe(1);
      expect(textareaEls[0].componentInstance.id).toBe('additionalInfo');
      expect(inputIds).toContain('surveyComment');
      expect(inputIds).toContain('linkedInLink');
      expect(selectEls.length).toBe(1);
      expect(selectEls[0].componentInstance.id).toBe('surveyTypeId');
      expect(selectEls[0].nativeElement.classList).toContain('tc-select');
    });

    it('should hide survey fields for the US-Afghan flow', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({
        surveyResponse: {
          surveyType: {id: US_AFGHAN_SURVEY_TYPE}
        }
      });

      expect(fixture.debugElement.queryAll(By.directive(NgSelectStubComponent)).length).toBe(0);
      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);
      expect(inputIds).not.toContain('surveyComment');
      expect(inputIds).toContain('linkedInLink');
    });
  });

  describe('validation', () => {
    beforeEach(async () => configureAndCreate());

    it('should expose allNotifications from the getter', () => {
      component.form.patchValue({allNotifications: true});

      expect(component.allNotifications).toBeTrue();
    });

    it('should validate the LinkedIn pattern', () => {
      component.form.patchValue({linkedInLink: 'https://example.com/not-linkedin'});

      expect(component.form.controls.linkedInLink.invalid).toBeTrue();
    });
  });

  describe('save flows', () => {
    beforeEach(async () => configureAndCreate());

    it('should update survey and other info then navigate next', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.save('next');

      expect(candidateServiceSpy.updateCandidateSurvey).toHaveBeenCalledWith(component.form.value);
      expect(candidateServiceSpy.updateCandidateOtherInfo).toHaveBeenCalledWith(component.form.value);
      expect(onSaveSpy).toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
      expect(component.saving).toBeFalse();
    });

    it('should update survey and other info then navigate back', () => {
      component.save('back');

      expect(candidateServiceSpy.updateCandidateSurvey).toHaveBeenCalledWith(component.form.value);
      expect(candidateServiceSpy.updateCandidateOtherInfo).toHaveBeenCalledWith(component.form.value);
      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should only update other info in the US-Afghan flow', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({
        surveyResponse: {
          surveyType: {id: US_AFGHAN_SURVEY_TYPE}
        }
      });

      component.save('next');

      expect(candidateServiceSpy.updateCandidateSurvey).not.toHaveBeenCalled();
      expect(candidateServiceSpy.updateCandidateOtherInfo).toHaveBeenCalledWith(component.form.value);
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should set error and clear saving when survey save fails', async () => {
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      await configureAndCreate({updateSurveyError: serverError});

      component.save('next');

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
      expect(candidateServiceSpy.updateCandidateOtherInfo).not.toHaveBeenCalled();
      expect(registrationServiceSpy.next).not.toHaveBeenCalled();
    });

    it('should set error and clear saving when other info save fails', async () => {
      TestBed.resetTestingModule();
      const serverError = {status: 503};
      await configureAndCreate({updateOtherInfoError: serverError});

      component.save('next');

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
    it('should set error when candidate survey fails to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({candidateSurveyError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeTrue();
    });

    it('should set error when additional info fails to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({additionalInfoError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error when survey types fail to load', async () => {
      const serverError = {status: 502};
      await configureAndCreate({surveyTypesError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });
});
