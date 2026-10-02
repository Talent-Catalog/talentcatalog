/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {CandidateJobExperienceComponent} from './candidate-job-experience.component';
import {CandidateJobExperience} from '../../../../../../model/candidate-job-experience';
import {ExtendDatePipe} from '../../../../../../util/date-adapter/extend-date-pipe';

function experienceFixture(): CandidateJobExperience {
  return {
    id: 456,
    country: {id: 1, name: 'Australia', status: 'active', translatedName: null},
    companyName: 'Acme Pty Ltd',
    role: 'Senior Software Engineer',
    startDate: '2020-01-01',
    endDate: '2022-06-01',
    fullTime: true as any,
    paid: true as any,
    description: '<p>Built <b>backend</b> services.</p>',
    tidiedDescription: 'Built backend services in Java.',
    keywordsInDescription: ['Java', 'Spring']
  };
}

@Component({
  template: `
    <app-candidate-job-experience [experience]="experience">
      <div experienceActions class="col-2 test-actions">Actions</div>
    </app-candidate-job-experience>
  `
})
class ActionsHostComponent {
  experience = experienceFixture();
}

describe('CandidateJobExperienceComponent', () => {
  let fixture: ComponentFixture<CandidateJobExperienceComponent>;
  let component: CandidateJobExperienceComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CandidateJobExperienceComponent, ActionsHostComponent, ExtendDatePipe],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateJobExperienceComponent);
    component = fixture.componentInstance;
  });

  function render(experience: CandidateJobExperience) {
    component.experience = experience;
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('should display the role', () => {
    const el = render(experienceFixture());

    expect(el.querySelector('h6.card-title').textContent).toContain('Senior Software Engineer');
  });

  it('should render the original description as HTML', () => {
    const el = render(experienceFixture());

    const original = Array.from(el.querySelectorAll('.form-control-plaintext'))[0] as HTMLElement;
    expect(original.querySelector('b').textContent).toBe('backend');
    expect(el.textContent).toContain('Original');
  });

  it('should display the tidied description', () => {
    const el = render(experienceFixture());

    expect(el.textContent).toContain('Tidied');
    expect(el.textContent).toContain('Built backend services in Java.');
  });

  it('should display keywords as badges', () => {
    const el = render(experienceFixture());

    const badges = Array.from(el.querySelectorAll('.badge')).map(b => b.textContent.trim());
    expect(badges).toEqual(['Java', 'Spring']);
  });

  it('should omit original, tidied and keywords sections when absent', () => {
    const el = render({
      ...experienceFixture(),
      description: '',
      tidiedDescription: undefined,
      keywordsInDescription: []
    });

    expect(el.textContent).not.toContain('Original');
    expect(el.textContent).not.toContain('Tidied');
    expect(el.textContent).not.toContain('Keywords');
  });

  it('should display start and end dates', () => {
    const el = render(experienceFixture());

    const pipe = new ExtendDatePipe('en-US');
    expect(el.textContent).toContain(pipe.transform('2020-01-01', 'customMonthYear'));
    expect(el.textContent).toContain(pipe.transform('2022-06-01', 'customMonthYear'));
  });

  it('should display full-time and paid', () => {
    const el = render(experienceFixture());

    expect(el.textContent).toContain('Full Time');
    expect(el.textContent).toContain('Paid');
  });

  it('should display part-time and voluntary', () => {
    const el = render({...experienceFixture(), fullTime: false as any, paid: false as any});

    expect(el.textContent).toContain('Part Time');
    expect(el.textContent).toContain('Voluntary');
  });

  it('should display the company and country', () => {
    const el = render(experienceFixture());

    expect(el.querySelector('.font-italic').textContent).toContain('Acme Pty Ltd, Australia');
  });

  it('should render no edit/delete controls of its own', () => {
    const el = render(experienceFixture());

    expect(el.querySelector('tc-button')).toBeNull();
    expect(el.querySelector('button')).toBeNull();
  });

  it('should project caller-supplied experienceActions content beside the details', () => {
    const hostFixture = TestBed.createComponent(ActionsHostComponent);
    hostFixture.detectChanges();

    const experienceEl = hostFixture.debugElement
      .query(By.directive(CandidateJobExperienceComponent)).nativeElement as HTMLElement;
    const actions = experienceEl.querySelector('.row > .test-actions');
    expect(actions).not.toBeNull();
    expect(actions.textContent).toContain('Actions');
  });
});
