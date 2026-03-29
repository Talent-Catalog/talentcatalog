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

import {Component, Input} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {TranslateModule} from '@ngx-translate/core';

import {CandidateEducationCardComponent} from './candidate-education-card.component';
import {CandidateEducation} from '../../../model/candidate-education';
import {Country} from '../../../model/country';
import {EducationMajor} from '../../../model/education-major';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
}

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

function makeEducation(overrides: Partial<CandidateEducation> = {}): CandidateEducation {
  return {
    id: 1,
    educationType: 'Bachelor',
    lengthOfCourseYears: 4,
    institution: 'Example University',
    courseName: 'Computer Science',
    yearCompleted: '2024',
    country: makeCountry(1, 'Jordan'),
    educationMajor: makeMajor(10, 'Engineering'),
    ...overrides
  };
}

describe('CandidateEducationCardComponent', () => {
  let component: CandidateEducationCardComponent;
  let fixture: ComponentFixture<CandidateEducationCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    disabled?: boolean;
    candidateEducation?: CandidateEducation;
    countries?: Country[];
    majors?: EducationMajor[];
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        CandidateEducationCardComponent,
        TcButtonStubComponent,
        TcDescriptionListStubComponent,
        TcDescriptionItemStubComponent
      ],
      imports: [TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateEducationCardComponent);
    component = fixture.componentInstance;
    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;
    component.candidateEducation = options?.candidateEducation ?? makeEducation();
    component.countries = options?.countries ?? [makeCountry(1, 'Jordan')];
    component.majors = options?.majors ?? [makeMajor(10, 'Engineering')];

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should render tc-button actions when not in preview mode', async () => {
      await configureAndCreate();

      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));
      expect(buttons.length).toBe(2);
      expect(buttons[0].componentInstance.color).toBe('error');
      expect(buttons[1].componentInstance.color).toBe('info');
    });

    it('should render the migrated description list details', async () => {
      await configureAndCreate({preview: true});

      const items = fixture.debugElement.queryAll(By.directive(TcDescriptionItemStubComponent));
      const labels = items.map(debugEl => debugEl.componentInstance.label);
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(labels).toContain('Education Type');
      expect(labels).toContain('Institution');
      expect(labels).toContain('Major');
      expect(text).toContain('Computer Science');
      expect(text).toContain('Example University');
      expect(text).toContain('Jordan');
      expect(text).toContain('CARD.EDUCATION.MAJOR');
    });
  });

  describe('events', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onEdit with the current education', () => {
      const onEditSpy = spyOn(component.onEdit, 'emit');

      component.edit();

      expect(onEditSpy).toHaveBeenCalledWith(component.candidateEducation);
    });

    it('should emit onDelete when delete is called', () => {
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.delete();

      expect(onDeleteSpy).toHaveBeenCalled();
    });
  });

  describe('helpers', () => {
    beforeEach(async () => configureAndCreate());

    it('should return the matching country name by id', () => {
      expect(component.getCountryName(makeCountry(1, 'Ignored'))).toBe('Jordan');
    });

    it('should return undefined when the country is not found', () => {
      expect(component.getCountryName(makeCountry(999, 'Missing'))).toBeUndefined();
    });

    it('should return the matching major name by id', () => {
      expect(component.getMajorName(makeMajor(10, 'Ignored'))).toBe('Engineering');
    });

    it('should return undefined when the major is not found', () => {
      expect(component.getMajorName(makeMajor(999, 'Missing'))).toBeUndefined();
    });
  });
});
