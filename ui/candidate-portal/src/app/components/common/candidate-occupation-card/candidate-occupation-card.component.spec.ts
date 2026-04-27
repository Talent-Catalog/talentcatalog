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

import {Component, Input, forwardRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';

import {CandidateOccupationCardComponent} from './candidate-occupation-card.component';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {Occupation} from '../../../model/occupation';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
}

@Component({
  selector: 'tc-label',
  template: '<ng-content></ng-content>'
})
class TcLabelStubComponent {}

@Component({
  selector: 'tc-description-list',
  template: '<ng-content></ng-content>'
})
class TcDescriptionListStubComponent {
  @Input() direction?: string;
  @Input() compact?: boolean;
  @Input() size?: string;
}

@Component({
  selector: 'tc-description-item',
  template: '<ng-content></ng-content>'
})
class TcDescriptionItemStubComponent {
  @Input() label?: string;
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
  @Input() type?: string;
  @Input() min?: number | string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'ng-select',
  template: '<ng-content></ng-content>',
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
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'ng-option',
  template: '<ng-content></ng-content>'
})
class NgOptionStubComponent {
  @Input() value?: unknown;
}

function makeOccupation(id: number, name: string): Occupation {
  return {id, name};
}

function makeCandidateOccupation(overrides: Partial<CandidateOccupation> = {}): CandidateOccupation {
  return {
    id: 1,
    occupation: makeOccupation(1, 'Engineer'),
    occupationId: 1,
    yearsExperience: 4,
    migrationOccupation: '',
    ...overrides
  };
}

describe('CandidateOccupationCardComponent', () => {
  let component: CandidateOccupationCardComponent;
  let fixture: ComponentFixture<CandidateOccupationCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    disabled?: boolean;
    candidateOccupation?: CandidateOccupation;
    candidateOccupations?: CandidateOccupation[];
    occupations?: Occupation[];
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        CandidateOccupationCardComponent,
        TcButtonStubComponent,
        TcLabelStubComponent,
        TcDescriptionListStubComponent,
        TcDescriptionItemStubComponent,
        TcInputStubComponent,
        NgSelectStubComponent,
        NgOptionStubComponent
      ],
      imports: [FormsModule, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateOccupationCardComponent);
    component = fixture.componentInstance;
    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;
    component.candidateOccupation = options?.candidateOccupation ?? makeCandidateOccupation();
    component.candidateOccupations = options?.candidateOccupations ?? [component.candidateOccupation];
    component.occupations = options?.occupations ?? [
      makeOccupation(0, 'Unknown'),
      makeOccupation(1, 'Engineer'),
      makeOccupation(2, 'Teacher')
    ];

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('template tc components', () => {
    it('should render tc-label, ng-select.tc-select, tc-input, and a delete tc-button in edit mode', async () => {
      await configureAndCreate();

      const nativeElement = fixture.nativeElement as HTMLElement;
      const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));
      const labels = fixture.debugElement.queryAll(By.directive(TcLabelStubComponent));
      const select = fixture.debugElement.query(By.directive(NgSelectStubComponent));
      const input = fixture.debugElement.query(By.directive(TcInputStubComponent));

      expect(button.componentInstance.color).toBe('error');
      expect(labels.length).toBeGreaterThan(0);
      expect(select.componentInstance.id).toBe('occupationId');
      expect(select.nativeElement.classList).toContain('tc-select');
      expect(input.componentInstance.type).toBe('number');
      expect(nativeElement.querySelectorAll('tc-description-list').length).toBe(0);
    });

    it('should render tc-description-list items in preview mode', async () => {
      await configureAndCreate({preview: true});

      const items = fixture.debugElement.queryAll(By.directive(TcDescriptionItemStubComponent));
      const labels = items.map(debugEl => debugEl.componentInstance.label);
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(labels).toContain('REGISTRATION.OCCUPATION.LABEL.OCCUPATION');
      expect(labels).toContain('REGISTRATION.OCCUPATION.LABEL.YEARSEXPERIENCE');
      expect(text).toContain('Engineer');
      expect(text).toContain('4');
    });
  });

  describe('behaviour', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onDelete when delete is called', () => {
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.delete();

      expect(onDeleteSpy).toHaveBeenCalled();
    });

    it('should return the matching occupation name', () => {
      expect(component.getOccupationName(makeOccupation(1, 'Ignored'))).toBe('Engineer');
    });

    it('should filter out already selected occupations and unknown when not current', () => {
      component.candidateOccupations = [
        makeCandidateOccupation({id: 1, occupationId: 1, occupation: makeOccupation(1, 'Engineer')}),
        makeCandidateOccupation({id: 2, occupationId: 2, occupation: makeOccupation(2, 'Teacher')})
      ];
      component.candidateOccupation = component.candidateOccupations[0];

      const ids = component.filteredOccupations.map(occupation => occupation.id);

      expect(ids).toContain(1);
      expect(ids).not.toContain(2);
      expect(ids).not.toContain(0);
    });
  });
});
