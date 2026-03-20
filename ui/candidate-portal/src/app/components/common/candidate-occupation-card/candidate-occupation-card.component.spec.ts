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

import {Component, EventEmitter, forwardRef, Input, NO_ERRORS_SCHEMA, Output, SimpleChange} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslateModule, TranslateService} from '@ngx-translate/core';

import {CandidateOccupationCardComponent} from './candidate-occupation-card.component';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {Occupation} from '../../../model/occupation';

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
  @Input() clearable?: boolean;
  @Input() placeholder?: string;
  @Input() items?: unknown[];
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

describe('CandidateOccupationCardComponent', () => {
  let component: CandidateOccupationCardComponent;
  let fixture: ComponentFixture<CandidateOccupationCardComponent>;

  async function configureAndCreate(options?: {
    candidateOccupation?: CandidateOccupation;
    candidateOccupations?: CandidateOccupation[];
    occupations?: Occupation[];
    preview?: boolean;
    disabled?: boolean;
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        CandidateOccupationCardComponent,
        TcInputStubComponent,
        NgSelectStubComponent
      ],
      imports: [
        FormsModule,
        TranslateModule.forRoot()
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateOccupationCardComponent);
    component = fixture.componentInstance;

    const candidateOccupation = options?.candidateOccupation ?? makeCandidateOccupation(1, 2, 5);
    component.candidateOccupation = candidateOccupation;
    component.candidateOccupations = options?.candidateOccupations ?? [candidateOccupation];
    component.occupations = options && 'occupations' in options ? options.occupations : [
      makeOccupation(1, 'Teacher'),
      makeOccupation(2, 'Engineer'),
      makeOccupation(0, 'Unknown')
    ];
    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;

    const translateService = TestBed.inject(TranslateService);
    translateService.use('en');

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should derive occupationId from the nested occupation object', async () => {
      const candidateOccupation = {
        id: 1,
        occupation: makeOccupation(2, 'Engineer'),
        yearsExperience: 5
      } as CandidateOccupation;

      await configureAndCreate({
        candidateOccupation,
        candidateOccupations: [candidateOccupation],
        preview: true
      });

      component.ngOnChanges({
        candidateOccupation: new SimpleChange(null, candidateOccupation, true)
      });

      expect(component.candidateOccupation.occupationId).toBe(2);
    });
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render tc-input for yearsExperience in edit mode', () => {
      const inputIds = fixture.debugElement
        .queryAll(By.directive(TcInputStubComponent))
        .map(debugEl => debugEl.componentInstance.id);

      expect(inputIds).toContain('yearsExperience');
    });

    it('should render ng-select with the tc-select class in edit mode', () => {
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

  describe('preview mode', () => {
    beforeEach(async () => configureAndCreate({preview: true}));

    it('should not render editable controls in preview mode', () => {
      expect(fixture.debugElement.queryAll(By.directive(TcInputStubComponent)).length).toBe(0);
      expect(fixture.debugElement.queryAll(By.directive(NgSelectStubComponent)).length).toBe(0);
    });

    it('should render the occupation name in preview mode', () => {
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('Engineer');
      expect(text).toContain('5');
    });
  });

  describe('unknown occupation display', () => {
    it('should show the migrated occupation text when occupationId is 0', async () => {
      await configureAndCreate({
        candidateOccupation: makeCandidateOccupation(1, 0, 4, 'Tailor')
      });

      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('Tailor');
      expect(text).toContain('ERROR.UNKNOWN_OCCUPATION');
    });
  });

  describe('filteredOccupations', () => {
    it('should exclude already selected occupations and the unknown occupation', async () => {
      await configureAndCreate({
        candidateOccupation: makeCandidateOccupation(1, 2, 5),
        candidateOccupations: [
          makeCandidateOccupation(1, 2, 5),
          makeCandidateOccupation(2, 1, 3)
        ]
      });

      const filteredIds = component.filteredOccupations.map(occupation => occupation.id);

      expect(filteredIds).not.toContain(1);
      expect(filteredIds).not.toContain(0);
      expect(filteredIds).toContain(2);
    });

    it('should include the unknown occupation when the current occupation is unknown', async () => {
      await configureAndCreate({
        candidateOccupation: makeCandidateOccupation(1, 0, 5, 'Tailor'),
        candidateOccupations: [makeCandidateOccupation(1, 0, 5, 'Tailor')]
      });

      const filteredIds = component.filteredOccupations.map(occupation => occupation.id);

      expect(filteredIds).toContain(0);
    });

    it('should return an empty array when occupations are unavailable', async () => {
      await configureAndCreate({occupations: undefined});

      expect(component.filteredOccupations).toEqual([]);
    });
  });

  describe('getOccupationName', () => {
    it('should return the matching occupation name', async () => {
      await configureAndCreate();

      expect(component.getOccupationName(makeOccupation(2, 'ignored'))).toBe('Engineer');
    });
  });

  describe('delete', () => {
    it('should emit onDelete', async () => {
      await configureAndCreate();
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.delete();

      expect(onDeleteSpy).toHaveBeenCalled();
    });
  });
});
