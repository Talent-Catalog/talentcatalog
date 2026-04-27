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

import {CandidateExamCardComponent} from './candidate-exam-card.component';
import {CandidateExam} from '../../../model/candidate';

function makeExam(overrides: Partial<CandidateExam> = {}): CandidateExam {
  return {
    id: 1,
    exam: 'IELTSGen' as any,
    otherExam: null,
    score: '7.5',
    year: 2024,
    notes: 'Strong result',
    ...overrides
  };
}

describe('CandidateExamCardComponent', () => {
  let component: CandidateExamCardComponent;
  let fixture: ComponentFixture<CandidateExamCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    disabled?: boolean;
    exam?: CandidateExam;
  }) {
    await TestBed.configureTestingModule({
      declarations: [CandidateExamCardComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateExamCardComponent);
    component = fixture.componentInstance;

    component.preview = options?.preview ?? false;
    component.disabled = options?.disabled ?? false;
    component.exam = options?.exam ?? makeExam();

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

    it('should render exam details', async () => {
      await configureAndCreate();
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(text).toContain('IELTS General');
      expect(text).toContain('2024');
      expect(text).toContain('7.5');
      expect(text).toContain('Strong result');
    });
  });

  describe('events', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onEdit with the current exam', () => {
      const onEditSpy = spyOn(component.onEdit, 'emit');

      component.editExam();

      expect(onEditSpy).toHaveBeenCalledWith(component.exam);
    });

    it('should emit onDelete with the current exam', () => {
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.deleteExam();

      expect(onDeleteSpy).toHaveBeenCalledWith(component.exam);
    });
  });

  describe('helpers', () => {
    beforeEach(async () => configureAndCreate());

    it('should return the matching exam label', () => {
      expect(component.getExamName('IELTSGen')).toBe('IELTS General');
    });

    it('should return Unknown when the exam is not found', () => {
      expect(component.getExamName('MissingExam')).toBe('Unknown');
    });
  });
});
