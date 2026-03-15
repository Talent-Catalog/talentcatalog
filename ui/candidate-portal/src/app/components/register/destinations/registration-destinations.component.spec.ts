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

import {Component, EventEmitter, Input, Output, QueryList} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationDestinationsComponent} from './registration-destinations.component';
import {Candidate, CandidateDestination, YesNoUnsureLearn} from '../../../model/candidate';
import {Country} from '../../../model/country';
import {CandidateService} from '../../../services/candidate.service';
import {RegistrationService} from '../../../services/registration.service';
import {CountryService} from '../../../services/country.service';
import {DestinationComponent} from './destination/destination.component';
import {CandidateDestinationService} from '../../../services/candidate-destination.service';

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
  selector: 'app-destination',
  template: ''
})
class DestinationStubComponent {
  @Input() candidateDestination?: CandidateDestination;
  @Input() country?: Country;
  @Input() saving?: boolean;
}

function makeCountry(id: number, name: string): Country {
  return {
    id,
    name,
    isoCode: name.slice(0, 2).toUpperCase(),
    status: 'active',
    translatedName: name
  };
}

function makeCandidateDestination(id: number, country: Country): CandidateDestination {
  return {
    id,
    interest: YesNoUnsureLearn.Yes,
    notes: `Notes for ${country.name}`,
    country
  };
}

function makeCandidate(destinations: CandidateDestination[] = []): Candidate {
  return {
    id: 7,
    candidateDestinations: destinations
  } as Candidate;
}

describe('RegistrationDestinationsComponent', () => {
  let component: RegistrationDestinationsComponent;
  let fixture: ComponentFixture<RegistrationDestinationsComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let candidateDestinationServiceSpy: jasmine.SpyObj<CandidateDestinationService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;

  async function configureAndCreate(options?: {
    destinations?: Country[];
    candidateDestinations?: CandidateDestination[];
    countriesError?: unknown;
    candidateError?: unknown;
    createError?: unknown;
    updateError?: unknown;
    edit?: boolean;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['getCandidateDestinations']);
    countryServiceSpy = jasmine.createSpyObj('CountryService', ['listTCDestinations']);
    candidateDestinationServiceSpy = jasmine.createSpyObj('CandidateDestinationService', ['create', 'update']);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);

    const destinations = options?.destinations ?? [
      makeCountry(1, 'Canada'),
      makeCountry(2, 'Australia')
    ];
    const candidateDestinations = options?.candidateDestinations ?? [
      makeCandidateDestination(10, destinations[0])
    ];

    if (options?.countriesError) {
      countryServiceSpy.listTCDestinations.and.returnValue(throwError(options.countriesError));
    } else {
      countryServiceSpy.listTCDestinations.and.returnValue(of(destinations));
    }

    if (options?.candidateError) {
      candidateServiceSpy.getCandidateDestinations.and.returnValue(throwError(options.candidateError));
    } else {
      candidateServiceSpy.getCandidateDestinations.and.returnValue(of(makeCandidate(candidateDestinations)));
    }

    if (options?.createError) {
      candidateDestinationServiceSpy.create.and.returnValue(throwError(options.createError));
    } else {
      candidateDestinationServiceSpy.create.and.returnValue(of(candidateDestinations[0] ?? makeCandidateDestination(11, destinations[0])));
    }

    if (options?.updateError) {
      candidateDestinationServiceSpy.update.and.returnValue(throwError(options.updateError));
    } else {
      candidateDestinationServiceSpy.update.and.returnValue(of(candidateDestinations[0] ?? makeCandidateDestination(10, destinations[0])));
    }

    await TestBed.configureTestingModule({
      declarations: [
        RegistrationDestinationsComponent,
        TcLoadingStubComponent,
        ErrorStubComponent,
        RegistrationFooterStubComponent,
        DestinationStubComponent
      ],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: CandidateDestinationService, useValue: candidateDestinationServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationDestinationsComponent);
    component = fixture.componentInstance;
    component.edit = options?.edit ?? false;
    component.destinationFormComponents = new QueryList<DestinationComponent>();
    component.destinationFormComponents.reset([]);

    fixture.detectChanges();
  }

  function setDestinationForms(forms: any[]) {
    const components = forms.map(form => ({form})) as DestinationComponent[];
    component.destinationFormComponents = new QueryList<DestinationComponent>();
    component.destinationFormComponents.reset(components);
    component.destinationFormComponents.notifyOnChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    beforeEach(async () => configureAndCreate());

    it('should load destinations and candidate destinations', () => {
      expect(countryServiceSpy.listTCDestinations).toHaveBeenCalled();
      expect(candidateServiceSpy.getCandidateDestinations).toHaveBeenCalled();
      expect(component.destinations.length).toBe(2);
      expect(component.candidateDestinations.length).toBe(1);
      expect(component.loading).toBeFalse();
    });

    it('should fetch the matching destination by country id', () => {
      expect(component.fetchDestination(1)?.id).toBe(10);
      expect(component.fetchDestination(999)).toBeUndefined();
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-loading while using the migrated loading component', () => {
      const loadingEl = fixture.debugElement.query(By.directive(TcLoadingStubComponent));

      expect(loadingEl).toBeTruthy();
      expect(loadingEl.componentInstance.loading).toBe(component.loading);
    });

    it('should render a destination child component for each destination', () => {
      const destinationEls = fixture.debugElement.queryAll(By.directive(DestinationStubComponent));

      expect(destinationEls.length).toBe(2);
    });

    it('should pass the registration footer type based on edit mode', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({edit: true});

      const footer = fixture.debugElement.query(By.directive(RegistrationFooterStubComponent));

      expect(footer.componentInstance.type).toBe('update');
      expect(footer.componentInstance.nextDisabled).toBe(component.loading || component.saving || !component.validationPassed);
    });
  });

  describe('validationPassed', () => {
    beforeEach(async () => configureAndCreate());

    it('should return true when all destination forms are valid', () => {
      setDestinationForms([{invalid: false}, {invalid: false}]);

      expect(component.validationPassed).toBeTrue();
    });

    it('should return false when any destination form is invalid', () => {
      setDestinationForms([{invalid: false}, {invalid: true}]);

      expect(component.validationPassed).toBeFalse();
    });
  });

  describe('save flows', () => {
    beforeEach(async () => configureAndCreate());

    it('should save changed destination forms and navigate next', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');
      setDestinationForms([
        {
          dirty: true,
          value: {
            id: 10,
            countryId: 1,
            interest: YesNoUnsureLearn.Yes,
            notes: 'Updated'
          }
        },
        {
          dirty: false,
          value: {
            id: null,
            countryId: 2,
            interest: YesNoUnsureLearn.No,
            notes: 'Skip'
          }
        }
      ]);

      component.next();

      expect(candidateDestinationServiceSpy.update).toHaveBeenCalledWith(10, {
        interest: YesNoUnsureLearn.Yes,
        notes: 'Updated'
      });
      expect(candidateDestinationServiceSpy.create).not.toHaveBeenCalled();
      expect(onSaveSpy).toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
      expect(component.saving).toBeFalse();
    });

    it('should create new destination entries when a changed form has no id', () => {
      setDestinationForms([
        {
          dirty: true,
          value: {
            id: null,
            countryId: 2,
            interest: YesNoUnsureLearn.No,
            notes: 'Interested in moving'
          }
        }
      ]);

      component.next();

      expect(candidateDestinationServiceSpy.create).toHaveBeenCalledWith(7, {
        countryId: 2,
        interest: YesNoUnsureLearn.No,
        notes: 'Interested in moving'
      });
    });

    it('should go back after saving on back()', () => {
      component.destinationForms = [{
        id: 10,
        countryId: 1,
        interest: YesNoUnsureLearn.Yes,
        notes: 'Back path'
      }];

      component.back();

      expect(candidateDestinationServiceSpy.update).toHaveBeenCalled();
      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should navigate without saving when there are no changed destination forms', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');
      setDestinationForms([{dirty: false, value: {}}]);

      component.next();

      expect(candidateDestinationServiceSpy.create).not.toHaveBeenCalled();
      expect(candidateDestinationServiceSpy.update).not.toHaveBeenCalled();
      expect(onSaveSpy).toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
      expect(component.saving).toBeFalse();
    });

    it('should set error and not navigate when save fails on next()', async () => {
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      await configureAndCreate({updateError: serverError});
      setDestinationForms([
        {
          dirty: true,
          value: {
            id: 10,
            countryId: 1,
            interest: YesNoUnsureLearn.Yes,
            notes: 'Broken'
          }
        }
      ]);

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
    it('should set error and clear loading when candidate destinations fail to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({candidateError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });

    it('should keep loading true when destinations fail to load before candidate data resolves it', async () => {
      const serverError = {status: 503};
      await configureAndCreate({countriesError: serverError});

      expect(component.destinations).toBeUndefined();
      expect(component.loading).toBeFalse();
      expect(component.error).toBeUndefined();
    });
  });
});
