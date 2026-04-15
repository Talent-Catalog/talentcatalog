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

import {Component, forwardRef, NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationContactComponent} from './registration-contact.component';
import {CandidateService} from '../../../services/candidate.service';
import {CountryService} from '../../../services/country.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {RegistrationService} from '../../../services/registration.service';
import {Candidate} from '../../../model/candidate';
import {Country} from '../../../model/country';

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
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

const mockCountries: Country[] = [
  {id: 1, name: 'United States'} as Country,
  {id: 2, name: 'Canada'} as Country,
];

const mockCandidate: Partial<Candidate> = {
  phone: '+1234567890',
  whatsapp: '+0987654321',
  relocatedAddress: '123 Main St',
  relocatedCity: 'Springfield',
  relocatedState: 'IL',
  relocatedCountry: {id: 1, name: 'United States'} as Country,
  user: {email: 'test@example.com'} as any,
};

describe('RegistrationContactComponent', () => {
  let component: RegistrationContactComponent;
  let fixture: ComponentFixture<RegistrationContactComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;

  beforeEach(waitForAsync(() => {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['getCandidateContact', 'updateCandidateContact']);
    countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', ['isAuthenticated']);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next']);
    authenticationServiceSpy.isAuthenticated.and.returnValue(false);

    TestBed.configureTestingModule({
      declarations: [RegistrationContactComponent, TcInputStubComponent, NgSelectStubComponent],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CountryService, useValue: countryServiceSpy},
        {provide: AuthenticationService, useValue: authenticationServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ---------------------------------------------------------------------------
  // Basic creation
  // ---------------------------------------------------------------------------

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ---------------------------------------------------------------------------
  // ngOnInit – unauthenticated
  // ---------------------------------------------------------------------------

  describe('ngOnInit (unauthenticated)', () => {
    it('should set loading to false', () => {
      expect(component.loading).toBeFalse();
    });

    it('should set authenticated to false', () => {
      expect(component.authenticated).toBeFalse();
    });

    it('should build a form with email, phone and whatsapp controls', () => {
      expect(component.form.contains('email')).toBeTrue();
      expect(component.form.contains('phone')).toBeTrue();
      expect(component.form.contains('whatsapp')).toBeTrue();
    });

    it('should NOT call getCandidateContact', () => {
      expect(candidateServiceSpy.getCandidateContact).not.toHaveBeenCalled();
    });

    it('should NOT call listCountries', () => {
      expect(countryServiceSpy.listCountries).not.toHaveBeenCalled();
    });
  });

  // ---------------------------------------------------------------------------
  // ngOnInit – authenticated
  // ---------------------------------------------------------------------------

  describe('ngOnInit (authenticated)', () => {
    beforeEach(() => {
      authenticationServiceSpy.isAuthenticated.and.returnValue(true);
      countryServiceSpy.listCountries.and.returnValue(of(mockCountries));
      candidateServiceSpy.getCandidateContact.and.returnValue(of(mockCandidate as Candidate));

      fixture = TestBed.createComponent(RegistrationContactComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set authenticated to true', () => {
      expect(component.authenticated).toBeTrue();
    });

    it('should set loading to false after data loads', () => {
      expect(component.loading).toBeFalse();
    });

    it('should populate email, phone and whatsapp from candidate', () => {
      expect(component.form.value.email).toBe('test@example.com');
      expect(component.form.value.phone).toBe('+1234567890');
      expect(component.form.value.whatsapp).toBe('+0987654321');
    });

    it('should add relocated address form controls', () => {
      expect(component.form.contains('relocatedAddress')).toBeTrue();
      expect(component.form.contains('relocatedCity')).toBeTrue();
      expect(component.form.contains('relocatedState')).toBeTrue();
      expect(component.form.contains('relocatedCountryId')).toBeTrue();
    });

    it('should populate relocated fields from candidate', () => {
      expect(component.form.value.relocatedAddress).toBe('123 Main St');
      expect(component.form.value.relocatedCity).toBe('Springfield');
      expect(component.form.value.relocatedState).toBe('IL');
      expect(component.form.value.relocatedCountryId).toBe(1);
    });

    it('should store the countries list', () => {
      expect(component.countries).toEqual(mockCountries);
    });

    it('should store the candidate reference', () => {
      expect(component.candidate).toEqual(mockCandidate as Candidate);
    });
  });

  // ---------------------------------------------------------------------------
  // ngOnInit – null safety
  // ---------------------------------------------------------------------------

  describe('ngOnInit null safety', () => {
    it('should set email to empty string when candidate.user is null', () => {
      authenticationServiceSpy.isAuthenticated.and.returnValue(true);
      countryServiceSpy.listCountries.and.returnValue(of(mockCountries));
      candidateServiceSpy.getCandidateContact.and.returnValue(
        of({...mockCandidate, user: null} as Candidate)
      );

      fixture = TestBed.createComponent(RegistrationContactComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.form.value.email).toBe('');
    });

    it('should set relocatedCountryId to null when candidate.relocatedCountry is null', () => {
      authenticationServiceSpy.isAuthenticated.and.returnValue(true);
      countryServiceSpy.listCountries.and.returnValue(of(mockCountries));
      candidateServiceSpy.getCandidateContact.and.returnValue(
        of({...mockCandidate, relocatedCountry: null} as Candidate)
      );

      fixture = TestBed.createComponent(RegistrationContactComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.form.value.relocatedCountryId).toBeNull();
    });
  });

  // ---------------------------------------------------------------------------
  // ngOnInit – error handling
  // ---------------------------------------------------------------------------

  describe('ngOnInit error handling', () => {
    it('should set error and clear loading when getCandidateContact fails', () => {
      authenticationServiceSpy.isAuthenticated.and.returnValue(true);
      countryServiceSpy.listCountries.and.returnValue(of(mockCountries));
      candidateServiceSpy.getCandidateContact.and.returnValue(
        throwError({message: 'Load failed'})
      );

      fixture = TestBed.createComponent(RegistrationContactComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.error).toBeTruthy();
      expect(component.loading).toBeFalse();
    });

    it('should set error when listCountries fails', () => {
      authenticationServiceSpy.isAuthenticated.and.returnValue(true);
      countryServiceSpy.listCountries.and.returnValue(
        throwError({message: 'Countries failed'})
      );
      candidateServiceSpy.getCandidateContact.and.returnValue(of(mockCandidate as Candidate));

      fixture = TestBed.createComponent(RegistrationContactComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.error).toBeTruthy();
    });
  });

  // ---------------------------------------------------------------------------
  // Form accessors
  // ---------------------------------------------------------------------------

  describe('form value accessors', () => {
    it('email getter should reflect the current form value', () => {
      component.form.patchValue({email: 'hello@world.com'});
      expect(component.email).toBe('hello@world.com');
    });

    it('phone getter should reflect the current form value', () => {
      component.form.patchValue({phone: '+44123456789'});
      expect(component.phone).toBe('+44123456789');
    });

    it('whatsapp getter should reflect the current form value', () => {
      component.form.patchValue({whatsapp: '+44987654321'});
      expect(component.whatsapp).toBe('+44987654321');
    });
  });

  // ---------------------------------------------------------------------------
  // Form validation
  // ---------------------------------------------------------------------------

  describe('form validation', () => {
    it('should be invalid when email is empty', () => {
      component.form.patchValue({email: ''});
      expect(component.form.invalid).toBeTrue();
    });

    it('should be valid when email has a value', () => {
      component.form.patchValue({email: 'valid@example.com'});
      expect(component.form.valid).toBeTrue();
    });

    it('should not require phone', () => {
      component.form.patchValue({email: 'a@b.com', phone: ''});
      expect(component.form.valid).toBeTrue();
    });

    it('should not require whatsapp', () => {
      component.form.patchValue({email: 'a@b.com', whatsapp: ''});
      expect(component.form.valid).toBeTrue();
    });
  });

  // ---------------------------------------------------------------------------
  // cancel()
  // ---------------------------------------------------------------------------

  describe('cancel()', () => {
    it('should emit the onSave event', () => {
      const spy = spyOn(component.onSave, 'emit');
      component.cancel();
      expect(spy).toHaveBeenCalled();
    });
  });

  // ---------------------------------------------------------------------------
  // save() – authenticated
  // ---------------------------------------------------------------------------

  describe('save() when authenticated', () => {
    beforeEach(() => {
      authenticationServiceSpy.isAuthenticated.and.returnValue(true);
      countryServiceSpy.listCountries.and.returnValue(of(mockCountries));
      candidateServiceSpy.getCandidateContact.and.returnValue(of(mockCandidate as Candidate));

      fixture = TestBed.createComponent(RegistrationContactComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should call updateCandidateContact with the form value', () => {
      candidateServiceSpy.updateCandidateContact.and.returnValue(of({} as Candidate));
      component.save();
      expect(candidateServiceSpy.updateCandidateContact).toHaveBeenCalledWith(component.form.value);
    });

    it('should call registrationService.next() on success', () => {
      candidateServiceSpy.updateCandidateContact.and.returnValue(of({} as Candidate));
      component.save();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should emit onSave on success', () => {
      const spy = spyOn(component.onSave, 'emit');
      candidateServiceSpy.updateCandidateContact.and.returnValue(of({} as Candidate));
      component.save();
      expect(spy).toHaveBeenCalled();
    });

    it('should set error and clear saving flag on failure', () => {
      const err = {message: 'Save failed'};
      candidateServiceSpy.updateCandidateContact.and.returnValue(throwError(err));
      component.save();
      expect(component.error).toEqual(err);
      expect(component.saving).toBeFalse();
    });

    it('should NOT emit onSave on failure', () => {
      const spy = spyOn(component.onSave, 'emit');
      candidateServiceSpy.updateCandidateContact.and.returnValue(
        throwError({message: 'error'})
      );
      component.save();
      expect(spy).not.toHaveBeenCalled();
    });
  });

  // ---------------------------------------------------------------------------
  // save() – unauthenticated
  // ---------------------------------------------------------------------------

  describe('save() when NOT authenticated', () => {
    it('should NOT call updateCandidateContact', () => {
      component.save();
      expect(candidateServiceSpy.updateCandidateContact).not.toHaveBeenCalled();
    });

    it('should reset saving to false', () => {
      component.save();
      expect(component.saving).toBeFalse();
    });
  });

  // ---------------------------------------------------------------------------
  // @Input() edit
  // ---------------------------------------------------------------------------

  describe('@Input() edit', () => {
    it('should default to false', () => {
      expect(component.edit).toBeFalse();
    });

    it('should accept true when set before detectChanges', () => {
      authenticationServiceSpy.isAuthenticated.and.returnValue(true);
      countryServiceSpy.listCountries.and.returnValue(of(mockCountries));
      candidateServiceSpy.getCandidateContact.and.returnValue(of(mockCandidate as Candidate));

      fixture = TestBed.createComponent(RegistrationContactComponent);
      component = fixture.componentInstance;
      component.edit = true;
      fixture.detectChanges();

      expect(component.edit).toBeTrue();
    });
  });
});
