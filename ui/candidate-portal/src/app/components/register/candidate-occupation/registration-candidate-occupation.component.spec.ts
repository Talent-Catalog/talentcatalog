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
import {
  ControlValueAccessor,
  FormsModule,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule
} from '@angular/forms';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationCandidateOccupationComponent} from './registration-candidate-occupation.component';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';
import {Occupation} from '../../../model/occupation';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateOccupationService} from '../../../services/candidate-occupation.service';
import {OccupationService} from '../../../services/occupation.service';
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
  @Input() placeholder?: string;
  @Input() formControlName?: string;
  @Input() min?: number;

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
  @Input() searchable?: boolean;
  @Input() placeholder?: string;
  @Input() formControlName?: string;
  @Input() bindValue?: string;
  @Input() bindLabel?: string;
  @Input() multiple?: boolean | string;
  @Output() ngModelChange = new EventEmitter<unknown>();

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

function makeOccupation(id: number, name: string): Occupation {
  return {id, name};
}

function makeCandidateOccupation(
  id: number,
  occupationId: number,
  yearsExperience = 3,
  migrationOccupation?: string
): CandidateOccupation {
  return {
    id,
    occupation: makeOccupation(occupationId, `Occupation ${occupationId}`),
    occupationId,
    yearsExperience,
    migrationOccupation
  };
}

function makeJobExperience(occupationId: number): CandidateJobExperience {
  return {
    id: 11,
    companyName: 'ACME',
    role: 'Engineer',
    startDate: '2020-01-01',
    endDate: '2021-01-01',
    fullTime: 'true',
    paid: 'true',
    description: 'desc',
    candidateOccupation: {
      id: 99,
      occupation: makeOccupation(occupationId, `Occupation ${occupationId}`),
      yearsExperience: 4
    }
  };
}

describe('RegistrationCandidateOccupationComponent', () => {
  let component: RegistrationCandidateOccupationComponent;
  let fixture: ComponentFixture<RegistrationCandidateOccupationComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let occupationServiceSpy: jasmine.SpyObj<OccupationService>;
  let candidateOccupationServiceSpy: jasmine.SpyObj<CandidateOccupationService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;

  async function configureAndCreate(options?: {
    candidateOccupations?: CandidateOccupation[];
    occupations?: Occupation[];
    candidateOccupationError?: unknown;
    occupationListError?: unknown;
    jobExperiences?: CandidateJobExperience[];
    modalResult?: boolean;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', [
      'getCandidateCandidateOccupations',
      'getCandidateJobExperiences'
    ]);
    occupationServiceSpy = jasmine.createSpyObj('OccupationService', ['listOccupations']);
    candidateOccupationServiceSpy = jasmine.createSpyObj('CandidateOccupationService', ['updateCandidateOccupations']);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    const candidateOccupations = options?.candidateOccupations ?? [];
    const occupations = options?.occupations ?? [
      makeOccupation(1, 'Teacher'),
      makeOccupation(2, 'Engineer'),
      makeOccupation(0, 'Unknown')
    ];

    if (options?.candidateOccupationError) {
      candidateServiceSpy.getCandidateCandidateOccupations.and.returnValue(
        throwError(options.candidateOccupationError)
      );
    } else {
      candidateServiceSpy.getCandidateCandidateOccupations.and.returnValue(of({
        candidateOccupations
      } as any));
    }

    if (options?.occupationListError) {
      occupationServiceSpy.listOccupations.and.returnValue(
        throwError(options.occupationListError)
      );
    } else {
      occupationServiceSpy.listOccupations.and.returnValue(of(occupations));
    }

    candidateServiceSpy.getCandidateJobExperiences.and.returnValue(of({
      candidateJobExperiences: options?.jobExperiences ?? []
    } as any));

    candidateOccupationServiceSpy.updateCandidateOccupations.and.returnValue(of({} as any));
    modalServiceSpy.open.and.returnValue({
      result: Promise.resolve(options?.modalResult ?? true)
    } as any);

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationCandidateOccupationComponent,
        TcInputStubComponent,
        NgSelectStubComponent
      ],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: OccupationService, useValue: occupationServiceSpy},
        {provide: CandidateOccupationService, useValue: candidateOccupationServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy},
        {provide: Router, useValue: routerSpy},
        {provide: NgbModal, useValue: modalServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationCandidateOccupationComponent);
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
    beforeEach(async () => configureAndCreate({
      candidateOccupations: [makeCandidateOccupation(1, 2, 5)]
    }));

    it('should build the expected form controls', () => {
      expect(component.form.contains('id')).toBeTrue();
      expect(component.form.contains('occupationId')).toBeTrue();
      expect(component.form.contains('yearsExperience')).toBeTrue();
    });

    it('should load occupations and candidate occupations', () => {
      expect(candidateServiceSpy.getCandidateCandidateOccupations).toHaveBeenCalled();
      expect(occupationServiceSpy.listOccupations).toHaveBeenCalled();
      expect(component.candidateOccupations.length).toBe(1);
      expect(component.candidateOccupations[0].occupationId).toBe(2);
    });

    it('should hide the create form when occupations already exist', () => {
      expect(component.showForm).toBeFalse();
    });

    it('should set loading to false after initial data loads', () => {
      expect(component.loading).toBeFalse();
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-input for yearsExperience', () => {
      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('yearsExperience');
    });

    it('should render ng-select controls with the tc-select class', () => {
      const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
      const selectIds = selectEls.map(debugEl => debugEl.componentInstance.id);

      expect(selectIds).toContain('occupationId');
      selectEls.forEach(debugEl => {
        expect(debugEl.nativeElement.classList).toContain('tc-select');
      });
    });

    it('should render tc-label for the migrated fields', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;

      expect(nativeElement.querySelector('tc-label[for="occupationId"]')).toBeTruthy();
      expect(nativeElement.querySelector('tc-label[for="yearsExperience"]')).toBeTruthy();
    });
  });

  describe('filteredOccupations', () => {
    it('should exclude already selected occupations and the unknown occupation', async () => {
      await configureAndCreate({
        candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
        occupations: [
          makeOccupation(1, 'Teacher'),
          makeOccupation(2, 'Engineer'),
          makeOccupation(0, 'Unknown')
        ]
      });

      const filteredIds = component.filteredOccupations.map(occupation => occupation.id);

      expect(filteredIds).toContain(1);
      expect(filteredIds).not.toContain(2);
      expect(filteredIds).not.toContain(0);
    });

    it('should return all occupations when none are selected', async () => {
      const occupations = [makeOccupation(1, 'Teacher'), makeOccupation(2, 'Engineer')];
      await configureAndCreate({occupations});

      expect(component.filteredOccupations).toEqual(occupations);
    });
  });

  describe('addOccupation', () => {
    beforeEach(async () => configureAndCreate());

    it('should add the occupation and reset the form when valid', () => {
      component.form.patchValue({
        occupationId: 1,
        yearsExperience: 4
      });

      component.addOccupation();

      expect(component.candidateOccupations.length).toBe(1);
      expect(component.candidateOccupations[0].occupationId).toBe(1);
      expect(component.form.value.occupationId).toBeNull();
      expect(component.form.value.yearsExperience).toBeNull();
      expect(component.showForm).toBeTrue();
    });

    it('should not add an occupation when the form is invalid', () => {
      component.form.patchValue({
        occupationId: null,
        yearsExperience: null
      });

      component.addOccupation();

      expect(component.candidateOccupations.length).toBe(0);
      expect(component.showForm).toBeTrue();
    });
  });

  describe('save via next()', () => {
    beforeEach(async () => configureAndCreate());

    it('should add the current form occupation before saving when the form is valid', () => {
      component.form.patchValue({
        occupationId: 1,
        yearsExperience: 4
      });

      component.next();

      expect(candidateOccupationServiceSpy.updateCandidateOccupations).toHaveBeenCalledWith({
        updates: jasmine.arrayContaining([
          jasmine.objectContaining({occupationId: 1, yearsExperience: 4})
        ])
      });
    });

    it('should emit onSave and call registrationService.next() on success', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.form.patchValue({
        occupationId: 1,
        yearsExperience: 4
      });

      component.next();

      expect(onSaveSpy).toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should set an error and not save when an occupation has invalid yearsExperience', () => {
      component.candidateOccupations = [{
        id: 1,
        occupation: makeOccupation(2, 'Engineer'),
        occupationId: 2,
        yearsExperience: null
      }];

      component.next();

      expect(candidateOccupationServiceSpy.updateCandidateOccupations).not.toHaveBeenCalled();
      expect(component.error).toBe('You need to put in a years experience value (from 0 upwards).');
    });

    it('should set error on update failure', () => {
      const serverError = {status: 500};
      candidateOccupationServiceSpy.updateCandidateOccupations.and.returnValue(
        throwError(serverError)
      );

      component.form.patchValue({
        occupationId: 1,
        yearsExperience: 4
      });

      component.next();

      expect(component.error).toEqual(serverError);
      expect(registrationServiceSpy.next).not.toHaveBeenCalled();
    });
  });

  describe('back()', () => {
    beforeEach(async () => configureAndCreate());

    it('should save and call registrationService.back()', () => {
      component.form.patchValue({
        occupationId: 1,
        yearsExperience: 4
      });

      component.back();

      expect(candidateOccupationServiceSpy.updateCandidateOccupations).toHaveBeenCalled();
      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should set error and not navigate if save-on-back fails', () => {
      const serverError = {status: 503, message: 'Service Unavailable'};
      candidateOccupationServiceSpy.updateCandidateOccupations.and.returnValue(
        throwError(serverError)
      );

      component.form.patchValue({
        occupationId: 1,
        yearsExperience: 4
      });

      component.back();

      expect(component.error).toEqual(serverError);
      expect(registrationServiceSpy.back).not.toHaveBeenCalled();
    });
  });

  describe('deleteOccupation', () => {
    it('should remove the occupation immediately when no job experiences are linked', async () => {
      await configureAndCreate({
        candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
        jobExperiences: []
      });

      component.deleteOccupation(0, 2);

      expect(candidateServiceSpy.getCandidateJobExperiences).toHaveBeenCalled();
      expect(component.candidateOccupations.length).toBe(0);
      expect(modalServiceSpy.open).not.toHaveBeenCalled();
    });

    it('should open the delete modal when job experiences are linked', async () => {
      await configureAndCreate({
        candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
        jobExperiences: [makeJobExperience(2)],
        modalResult: true
      });

      component.deleteOccupation(0, 2);
      await Promise.resolve();

      expect(modalServiceSpy.open).toHaveBeenCalled();
      expect(component.candidateOccupations.length).toBe(0);
    });

    it('should keep the occupation when the delete modal resolves false', async () => {
      await configureAndCreate({
        candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
        jobExperiences: [makeJobExperience(2)],
        modalResult: false
      });

      component.deleteOccupation(0, 2);
      await Promise.resolve();

      expect(modalServiceSpy.open).toHaveBeenCalled();
      expect(component.candidateOccupations.length).toBe(1);
    });
  });

  describe('error paths', () => {
    it('should set error and stop loading if candidate occupations fail to load', async () => {
      const serverError = {status: 404};
      await configureAndCreate({candidateOccupationError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error and stop loading if occupation list fails to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({occupationListError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });
});
