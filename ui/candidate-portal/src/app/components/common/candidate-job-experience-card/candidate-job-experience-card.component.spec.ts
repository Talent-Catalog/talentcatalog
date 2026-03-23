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

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateModule, TranslateService} from '@ngx-translate/core';

import {CandidateJobExperienceCardComponent} from './candidate-job-experience-card.component';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';
import {CandidateOccupation} from '../../../model/candidate-occupation';
import {Country} from '../../../model/country';
import {Occupation} from '../../../model/occupation';

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
    yearsExperience: 4,
    occupationId
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
    description: 'Plain description',
    country: makeCountry(1, 'Jordan'),
    candidateOccupation: makeCandidateOccupation(10, 100, 'Engineer'),
    ...overrides
  };
}

describe('CandidateJobExperienceCardComponent', () => {
  let component: CandidateJobExperienceCardComponent;
  let fixture: ComponentFixture<CandidateJobExperienceCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    disabled?: boolean;
    experience?: CandidateJobExperience;
    countries?: Country[];
  }) {
    await TestBed.configureTestingModule({
      declarations: [CandidateJobExperienceCardComponent],
      imports: [TranslateModule.forRoot()],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateJobExperienceCardComponent);
    component = fixture.componentInstance;

    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;
    component.countries = options?.countries ?? [
      makeCountry(1, 'Jordan'),
      makeCountry(2, 'Lebanon')
    ];
    component.experience = options?.experience ?? makeExperience();

    const translateService = TestBed.inject(TranslateService);
    translateService.use('en');

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should render the role, company, and country name', async () => {
      await configureAndCreate();
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('Engineer');
      expect(text).toContain('ACME');
      expect(text).toContain('Jordan');
    });

    it('should render tc-button actions when not in preview mode', async () => {
      await configureAndCreate();
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(2);
    });

    it('should not render action buttons in preview mode', async () => {
      await configureAndCreate({preview: true});

      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(0);
    });

    it('should render full-time and paid translation keys for boolean flags', async () => {
      await configureAndCreate();
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('CARD.JOBEXPERIENCE.LABEL.FULLTIME');
      expect(text).toContain('CARD.JOBEXPERIENCE.LABEL.PAID');
    });

    it('should render part-time and volunteer translation keys when flags are false', async () => {
      await configureAndCreate({
        experience: makeExperience({
          fullTime: false as any,
          paid: false as any
        })
      });

      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('CARD.JOBEXPERIENCE.LABEL.PARTTIME');
      expect(text).toContain('CARD.JOBEXPERIENCE.LABEL.VOLUNTEER');
    });

    it('should render current when endDate is missing', async () => {
      await configureAndCreate({
        experience: makeExperience({
          endDate: null as any
        })
      });

      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('2020');
      expect(text).toContain('current');
    });
  });

  describe('edit', () => {
    it('should emit onEdit with the current experience', async () => {
      await configureAndCreate();
      const onEditSpy = spyOn(component.onEdit, 'emit');

      component.edit();

      expect(onEditSpy).toHaveBeenCalledWith(component.experience);
    });
  });

  describe('delete', () => {
    it('should emit onDelete with the current experience', async () => {
      await configureAndCreate();
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.delete();

      expect(onDeleteSpy).toHaveBeenCalledWith(component.experience);
    });
  });

  describe('getCountryName', () => {
    it('should return the matching country name by id', async () => {
      await configureAndCreate();

      expect(component.getCountryName(makeCountry(1, 'Ignored'))).toBe('Jordan');
    });

    it('should return undefined when the country is not found', async () => {
      await configureAndCreate();

      expect(component.getCountryName(makeCountry(999, 'Missing'))).toBeUndefined();
    });
  });

  describe('isHtml', () => {
    beforeEach(async () => configureAndCreate());

    it('should return true for HTML content', () => {
      expect(component.isHtml('<p>Hello</p>')).toBeTrue();
    });

    it('should return false for plain text', () => {
      expect(component.isHtml('Hello')).toBeFalse();
    });
  });
});
