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
import {CandidateCertificationCardComponent} from './candidate-certification-card.component';
import {CandidateCertification} from '../../../model/candidate-certification';

function makeCertification(overrides: Partial<CandidateCertification> = {}): CandidateCertification {
  return {
    id: 1,
    name: 'AWS Certified',
    institution: 'Amazon',
    dateCompleted: '2024-01-15',
    ...overrides
  };
}

describe('CandidateCertificationCardComponent', () => {
  let component: CandidateCertificationCardComponent;
  let fixture: ComponentFixture<CandidateCertificationCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    disabled?: boolean;
    certificate?: CandidateCertification;
  }) {
    await TestBed.configureTestingModule({
      declarations: [CandidateCertificationCardComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateCertificationCardComponent);
    component = fixture.componentInstance;
    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;
    component.certificate = options?.certificate ?? makeCertification();

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
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(2);
    });

    it('should not render tc-button actions in preview mode', async () => {
      await configureAndCreate({preview: true});
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(0);
    });

    it('should render certification details', async () => {
      await configureAndCreate();
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('AWS Certified');
      expect(text).toContain('Amazon');
      expect(text).toContain('2024');
    });
  });

  describe('events', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onEdit with the current certification', () => {
      const onEditSpy = spyOn(component.onEdit, 'emit');

      component.editCertificate();

      expect(onEditSpy).toHaveBeenCalledWith(component.certificate);
    });

    it('should emit onDelete with the current certification', () => {
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.deleteCertificate();

      expect(onDeleteSpy).toHaveBeenCalledWith(component.certificate);
    });
  });
});
