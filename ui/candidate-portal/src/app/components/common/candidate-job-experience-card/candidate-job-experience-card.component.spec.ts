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

import {CandidateJobExperienceCardComponent} from './candidate-job-experience-card.component';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';
import {Country} from '../../../model/country';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
}

function makeCountry(id: number, name: string): Country {
  return {
    id,
    name,
    status: 'active',
    translatedName: name
  };
}

function makeExperience(overrides: Partial<CandidateJobExperience> = {}): CandidateJobExperience {
  return {
    id: 1,
    companyName: 'ACME',
    role: 'Engineer',
    startDate: '2020-01-01',
    endDate: '2021-01-01',
    fullTime: true as any,
    paid: true as any,
    description: 'Built systems',
    country: makeCountry(1, 'Jordan'),
    candidateOccupation: null as any,
    ...overrides
  };
}

describe('CandidateWorkExperienceCardComponent', () => {
  let component: CandidateJobExperienceCardComponent;
  let fixture: ComponentFixture<CandidateJobExperienceCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    disabled?: boolean;
    experience?: CandidateJobExperience;
    countries?: Country[];
  }) {
    await TestBed.configureTestingModule({
      declarations: [CandidateJobExperienceCardComponent, TcButtonStubComponent],
      imports: [TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateJobExperienceCardComponent);
    component = fixture.componentInstance;
    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;
    component.experience = options?.experience ?? makeExperience();
    component.countries = options?.countries ?? [makeCountry(1, 'Jordan'), makeCountry(2, 'Lebanon')];

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should render delete and edit tc-buttons when not in preview mode', async () => {
      await configureAndCreate();

      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));

      expect(buttons.length).toBe(2);
      expect(buttons[0].componentInstance.color).toBe('error');
      expect(buttons[1].componentInstance.color).toBe('info');
    });

    it('should render experience details in the migrated layout', async () => {
      await configureAndCreate();
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('Engineer');
      expect(text).toContain('ACME');
      expect(text).toContain('Jordan');
      expect(text).toContain('CARD.JOBEXPERIENCE.LABEL.FULLTIME');
      expect(text).toContain('CARD.JOBEXPERIENCE.LABEL.PAID');
      expect(text).toContain('2020');
      expect(text).toContain('2021');
    });
  });

  describe('events', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onEdit with the current experience', () => {
      const onEditSpy = spyOn(component.onEdit, 'emit');

      component.edit();

      expect(onEditSpy).toHaveBeenCalledWith(component.experience);
    });

    it('should emit onDelete with the current experience', () => {
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.delete();

      expect(onDeleteSpy).toHaveBeenCalledWith(component.experience);
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

    it('should detect html content', () => {
      expect(component.isHtml('<p>Hello</p>')).toBeTrue();
      expect(component.isHtml('Hello')).toBeFalse();
    });
  });
});
