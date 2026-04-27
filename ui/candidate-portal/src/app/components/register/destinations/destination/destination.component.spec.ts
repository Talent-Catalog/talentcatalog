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

import {Component, forwardRef, Input} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';

import {DestinationComponent} from './destination.component';
import {RegistrationService} from '../../../../services/registration.service';
import {CandidateDestination, YesNoUnsureLearn} from '../../../../model/candidate';
import {Country} from '../../../../model/country';

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
  @Input() clearOnBackspace?: boolean;
  @Input() placeholder?: string;
  @Input() bindLabel?: string;
  @Input() bindValue?: string;
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
  selector: 'tc-label',
  template: '<ng-content></ng-content>'
})
class TcLabelStubComponent {
  @Input() for?: string;
}

describe('DestinationComponent', () => {
  let component: DestinationComponent;
  let fixture: ComponentFixture<DestinationComponent>;

  const mockCandidateDestination: CandidateDestination = {
    id: 1,
    interest: YesNoUnsureLearn.Yes,
    notes: 'Looking forward to this opportunity.'
  };

  const mockCountry: Country = {
    id: 101,
    name: 'Canada',
    isoCode: 'CA',
    status: 'active',
    translatedName: null
  };

  async function configureAndCreate(options?: {
    candidateDestination?: CandidateDestination | null;
    country?: Country;
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        DestinationComponent,
        NgSelectStubComponent,
        TcTextareaStubComponent,
        TcLabelStubComponent
      ],
      imports: [
        ReactiveFormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {provide: RegistrationService, useValue: {}}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DestinationComponent);
    component = fixture.componentInstance;
    component.candidateDestination = options?.candidateDestination === undefined
      ? mockCandidateDestination
      : options.candidateDestination;
    component.country = options?.country ?? mockCountry;
    component.saving = false;

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    beforeEach(async () => configureAndCreate());

    it('should initialize form with candidateDestination and country data', () => {
      const formValue = component.form.value;

      expect(formValue.id).toEqual(mockCandidateDestination.id);
      expect(formValue.countryId).toEqual(mockCountry.id);
      expect(formValue.interest).toEqual(mockCandidateDestination.interest);
      expect(formValue.notes).toEqual(mockCandidateDestination.notes);
    });

    it('should initialize an empty destination when no candidateDestination is provided', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({candidateDestination: null});

      expect(component.form.value.id).toBeNull();
      expect(component.form.value.countryId).toEqual(mockCountry.id);
      expect(component.form.value.interest).toBeNull();
      expect(component.form.value.notes).toBeNull();
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-label elements for the migrated fields', () => {
      const labelFors = fixture.debugElement
        .queryAll(By.directive(TcLabelStubComponent))
        .map(debugEl => debugEl.componentInstance.for);

      expect(labelFors).toContain('interest101');
      expect(labelFors).toContain('notes101');
    });

    it('should render the interest ng-select with the tc-select class', () => {
      const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));

      expect(selectEls.length).toBe(1);
      expect(selectEls[0].componentInstance.id).toBe('interest101');
      expect(selectEls[0].nativeElement.classList).toContain('tc-select');
    });

    it('should render the notes tc-textarea when interest is selected', () => {
      const textareaEls = fixture.debugElement.queryAll(By.directive(TcTextareaStubComponent));

      expect(textareaEls.length).toBe(1);
      expect(textareaEls[0].componentInstance.id).toBe('notes101');
    });

    it('should hide the notes field when interest is not selected', () => {
      component.form.patchValue({interest: null});
      fixture.detectChanges();

      expect(fixture.debugElement.queryAll(By.directive(TcTextareaStubComponent)).length).toBe(0);
    });
  });

  describe('validation', () => {
    beforeEach(async () => configureAndCreate());

    it('should return correct interest value from getter', () => {
      expect(component.interest).toEqual(mockCandidateDestination.interest);
    });

    it('should mark interest as invalid when not provided', () => {
      component.form.controls['interest'].setValue(null);

      expect(component.form.controls['interest'].valid).toBeFalse();
    });

    it('should mark interest as valid when provided', () => {
      component.form.controls['interest'].setValue(YesNoUnsureLearn.No);

      expect(component.form.controls['interest'].valid).toBeTrue();
    });
  });
});
