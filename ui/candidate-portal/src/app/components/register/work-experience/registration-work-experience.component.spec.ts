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

import {Component, EventEmitter, Input, NO_ERRORS_SCHEMA, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationWorkExperienceComponent} from './registration-work-experience.component';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {Country} from '../../../model/country';
import {Occupation} from '../../../model/occupation';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateJobExperienceService} from '../../../services/candidate-job-experience.service';
import {CountryService} from '../../../services/country.service';
import {CandidateOccupationService} from '../../../services/candidate-occupation.service';
import {RegistrationService} from '../../../services/registration.service';

@Component({
  selector: 'app-candidate-job-experience-form',
  template: ''
})
class CandidateJobExperienceFormStubComponent {
  @Input() countries?: Country[];
  @Input() candidateOccupation?: CandidateOccupation;
  @Input() candidateOccupations?: CandidateOccupation[];
  @Input() candidateJobExperience?: CandidateJobExperience;
  @Output() formSaved = new EventEmitter<CandidateJobExperience>();
  @Output() formClosed = new EventEmitter<CandidateJobExperience>();
}

@Component({
  selector: 'app-candidate-job-experience-card',
  template: ''
})
class CandidateJobExperienceCardStubComponent {
  @Input() experience?: CandidateJobExperience;
  @Input() countries?: Country[];
  @Output() onDelete = new EventEmitter<CandidateJobExperience>();
  @Output() onEdit = new EventEmitter<CandidateJobExperience>();
}

function makeCountry(id: number, name: string): Country {
  return {
    id,
    name,
    status: 'active',
    translatedName: name
  };
}

function makeOccupation(id: number, name: string): Occupation {
  return {id, name};
}

function makeCandidateOccupation(id: number, occupationId: number, name: string): CandidateOccupation {
  return {
    id,
    occupation: makeOccupation(occupationId, name),
    yearsExperience: 5,
    occupationId
  };
}

function makeJobExperience(id: number, candidateOccupation: CandidateOccupation, country: Country): CandidateJobExperience {
  return {
    id,
    companyName: `Company ${id}`,
    role: `Role ${id}`,
    startDate: '2020-01-01',
    endDate: '2021-01-01',
    fullTime: 'true',
    paid: 'true',
    description: 'desc',
    country,
    candidateOccupation
  };
}

describe('RegistrationWorkExperienceComponent', () => {
  let component: RegistrationWorkExperienceComponent;
  let fixture: ComponentFixture<RegistrationWorkExperienceComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let candidateOccupationServiceSpy: jasmine.SpyObj<CandidateOccupationService>;
  let jobExperienceServiceSpy: jasmine.SpyObj<CandidateJobExperienceService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  async function configureAndCreate(options?: {
    candidateJobExperiences?: CandidateJobExperience[];
    occupations?: CandidateOccupation[];
    countries?: Country[];
    candidateJobExperiencesError?: unknown;
    occupationListError?: unknown;
    countriesError?: unknown;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['getCandidateJobExperiences']);
    candidateOccupationServiceSpy = jasmine.createSpyObj('CandidateOccupationService', ['listMyOccupations']);
    jobExperienceServiceSpy = jasmine.createSpyObj('CandidateJobExperienceService', ['deleteJobExperience']);
    countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    const countries = options?.countries ?? [
      makeCountry(1, 'Jordan'),
      makeCountry(2, 'Lebanon')
    ];
    const occupations = options?.occupations ?? [
      makeCandidateOccupation(10, 100, 'Engineer'),
      makeCandidateOccupation(20, 200, 'Teacher')
    ];
    const candidateJobExperiences = options?.candidateJobExperiences ?? [
      makeJobExperience(1, occupations[0], countries[0])
    ];

    if (options?.candidateJobExperiencesError) {
      candidateServiceSpy.getCandidateJobExperiences.and.returnValue(
        throwError(options.candidateJobExperiencesError)
      );
    } else {
      candidateServiceSpy.getCandidateJobExperiences.and.returnValue(of({
        candidateJobExperiences
      } as any));
    }

    if (options?.occupationListError) {
      candidateOccupationServiceSpy.listMyOccupations.and.returnValue(
        throwError(options.occupationListError)
      );
    } else {
      candidateOccupationServiceSpy.listMyOccupations.and.returnValue(of(occupations));
    }

    if (options?.countriesError) {
      countryServiceSpy.listCountries.and.returnValue(
        throwError(options.countriesError)
      );
    } else {
      countryServiceSpy.listCountries.and.returnValue(of(countries));
    }

    jobExperienceServiceSpy.deleteJobExperience.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationWorkExperienceComponent,
        CandidateJobExperienceFormStubComponent,
        CandidateJobExperienceCardStubComponent
      ],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: Router, useValue: routerSpy},
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CandidateOccupationService, useValue: candidateOccupationServiceSpy},
        {provide: CandidateJobExperienceService, useValue: jobExperienceServiceSpy},
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationWorkExperienceComponent);
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

    it('should load candidate job experiences, occupations, and countries', () => {
      expect(candidateServiceSpy.getCandidateJobExperiences).toHaveBeenCalled();
      expect(candidateOccupationServiceSpy.listMyOccupations).toHaveBeenCalled();
      expect(countryServiceSpy.listCountries).toHaveBeenCalled();
      expect(component.candidateJobExperiences.length).toBe(1);
      expect(component.occupations.length).toBe(2);
      expect(component.countries.length).toBe(2);
    });

    it('should set loading to false after all data loads', () => {
      expect(component.loading).toBeFalse();
    });

    it('should populate experience map by candidate occupation', () => {
      expect(component.experiencesByCandidateOccupation[10].length).toBe(1);
      expect(component.experiencesByCandidateOccupation[20].length).toBe(0);
    });
  });

  describe('template', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-loading and tc-alert in the parent template', () => {
      const nativeElement = fixture.nativeElement as HTMLElement;

      expect(nativeElement.querySelector('tc-loading')).toBeTruthy();
      expect(nativeElement.querySelector('tc-alert')).toBeTruthy();
    });

    it('should render experience cards when the form is closed', () => {
      expect(fixture.debugElement.queryAll(By.directive(CandidateJobExperienceCardStubComponent)).length).toBe(1);
      expect(fixture.debugElement.queryAll(By.directive(CandidateJobExperienceFormStubComponent)).length).toBe(0);
    });

    it('should render the experience form when experienceFormOpen is true', () => {
      component.experienceFormOpen = true;
      fixture.detectChanges();

      expect(fixture.debugElement.queryAll(By.directive(CandidateJobExperienceFormStubComponent)).length).toBe(1);
    });
  });

  describe('navigation', () => {
    beforeEach(async () => configureAndCreate());

    it('should call registrationService.back()', () => {
      component.back();
      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should call registrationService.next()', () => {
      component.next();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should emit onSave when closeEdit is called', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.closeEdit();

      expect(onSaveSpy).toHaveBeenCalled();
    });
  });

  describe('experience form flow', () => {
    beforeEach(async () => configureAndCreate());

    it('should open the form for a selected occupation', () => {
      const occupation = component.occupations[0];

      component.addExperience(occupation);

      expect(component.occupation).toBe(occupation);
      expect(component.experienceFormOpen).toBeTrue();
    });

    it('should open the form for editing an existing experience', () => {
      const experience = component.candidateJobExperiences[0];

      component.handleEdit(experience);

      expect(component.experience).toBe(experience);
      expect(component.experienceFormOpen).toBeTrue();
    });

    it('should close the form and clear state on cancel', () => {
      component.experienceFormOpen = true;
      component.occupation = component.occupations[0];
      component.experience = component.candidateJobExperiences[0];

      component.handleCancelled(component.experience);

      expect(component.experienceFormOpen).toBeFalse();
      expect(component.occupation).toBeNull();
      expect(component.experience).toBeNull();
    });

    it('should append a new experience on save', () => {
      const occupation = component.occupations[1];
      const newExperience = makeJobExperience(2, occupation, component.countries[1]);

      component.handleSave(newExperience);

      expect(component.candidateJobExperiences.length).toBe(2);
      expect(component.experiencesByCandidateOccupation[20].length).toBe(1);
      expect(component.experienceFormOpen).toBeFalse();
    });

    it('should replace an existing experience on save when ids match', () => {
      const occupation = component.occupations[0];
      const updatedExperience = {
        ...makeJobExperience(1, occupation, component.countries[0]),
        companyName: 'Updated Company'
      };

      component.handleSave(updatedExperience);

      expect(component.candidateJobExperiences.length).toBe(1);
      expect(component.candidateJobExperiences[0].companyName).toBe('Updated Company');
      expect(component.experience).toBeNull();
      expect(component.experienceFormOpen).toBeFalse();
    });
  });

  describe('handleDelete', () => {
    beforeEach(async () => configureAndCreate());

    it('should delete the experience and refresh the map on success', () => {
      const experience = component.candidateJobExperiences[0];

      component.handleDelete(experience);

      expect(jobExperienceServiceSpy.deleteJobExperience).toHaveBeenCalledWith(experience.id);
      expect(component.candidateJobExperiences.length).toBe(0);
      expect(component.experiencesByCandidateOccupation[10].length).toBe(0);
      expect(component.saving).toBeFalse();
    });

    it('should set error and clear saving on delete failure', () => {
      const serverError = {status: 500};
      jobExperienceServiceSpy.deleteJobExperience.and.returnValue(
        throwError(serverError)
      );

      component.handleDelete(component.candidateJobExperiences[0]);

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });
  });

  describe('completedJobExperiences', () => {
    it('should return false when an occupation has no experiences', async () => {
      await configureAndCreate();

      // The default fixture loads two occupations, but only the first one has a mapped
      // job experience, so the second occupation keeps the completion check false.
      expect(component.completedJobExperiences()).toBeFalse();
    });

    it('should return true when every occupation has at least one experience', async () => {
      const countries = [makeCountry(1, 'Jordan'), makeCountry(2, 'Lebanon')];
      const occupations = [
        makeCandidateOccupation(10, 100, 'Engineer'),
        makeCandidateOccupation(20, 200, 'Teacher')
      ];

      await configureAndCreate({
        countries,
        occupations,
        candidateJobExperiences: [
          makeJobExperience(1, occupations[0], countries[0]),
          makeJobExperience(2, occupations[1], countries[1])
        ]
      });

      expect(component.completedJobExperiences()).toBeTrue();
    });
  });

  describe('error paths', () => {
    it('should set error and stop loading when candidate job experiences fail to load', async () => {
      const serverError = {status: 404};
      await configureAndCreate({candidateJobExperiencesError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component._loading.candidate).toBeFalse();
      expect(component.loading).toBeTrue();
    });

    it('should set error and stop loading when occupations fail to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({occupationListError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should set error and stop loading when countries fail to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({countriesError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });
});
