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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {Subject, of, throwError} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {CandidateMatchExplanationComponent} from './candidate-match-explanation.component';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateMatchExplanation} from '../../../model/candidate-match-explanation';

@Component({
  template: `
    <app-candidate-match-explanation
      [candidateId]="candidateId"
      [jobId]="jobId"
      [opportunityDescription]="opportunityDescription">
    </app-candidate-match-explanation>
  `
})
class TestHostComponent {
  candidateId = 1;
  jobId = 100;
  opportunityDescription = 'Initial opportunity description';
}

function notFoundError(): HttpErrorResponse {
  return new HttpErrorResponse({status: 404, statusText: 'Not Found'});
}

function serverError(): HttpErrorResponse {
  return new HttpErrorResponse({status: 500, statusText: 'Internal Server Error'});
}

function explanationFixture(summary: string): CandidateMatchExplanation {
  return {
    summary,
    experienceExplanations: [
      {experienceId: 501, explanation: 'Directly relevant Java experience.'},
      {experienceId: 502, explanation: 'Relevant leadership experience.'}
    ],
    limitations: ['Limited detail in job description.']
  };
}

describe('CandidateMatchExplanationComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let hostComponent: TestHostComponent;
  let component: CandidateMatchExplanationComponent;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CandidateService', [
      'getMatchExplanation', 'generateMatchExplanation'
    ]);

    await TestBed.configureTestingModule({
      declarations: [CandidateMatchExplanationComponent, TestHostComponent],
      providers: [
        {provide: CandidateService, useValue: spy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  function createHost() {
    hostFixture = TestBed.createComponent(TestHostComponent);
    hostComponent = hostFixture.componentInstance;
    hostFixture.detectChanges();
    component = hostFixture.debugElement
      .query(By.directive(CandidateMatchExplanationComponent)).componentInstance;
  }

  it('should receive the three required inputs', () => {
    candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
    createHost();

    expect(component.candidateId).toBe(1);
    expect(component.jobId).toBe(100);
    expect(component.opportunityDescription).toBe('Initial opportunity description');
  });

  describe('initial retrieval', () => {

    it('should retrieve an existing explanation via GET', fakeAsync(() => {
      const explanation = explanationFixture('Existing summary');
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
      createHost();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledWith(1, 100);
    }));

    it('should display the existing explanation', fakeAsync(() => {
      const explanation = explanationFixture('Existing summary');
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
      createHost();
      tick();

      expect(component.explanation).toEqual(explanation);
    }));

    it('should NOT trigger generation when GET succeeds', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should automatically generate when GET returns 404', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(notFoundError()));
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('Generated')));
      createHost();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalled();
    }));

    it('should display the generated explanation after a 404', fakeAsync(() => {
      const generated = explanationFixture('Generated summary');
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(notFoundError()));
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(generated));
      createHost();
      tick();

      expect(component.explanation).toEqual(generated);
    }));

    it('should generate using the current candidateId, jobId and opportunityDescription', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(notFoundError()));
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('G')));
      createHost();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalledWith(1, {
        jobId: 100,
        opportunityDescription: 'Initial opportunity description'
      });
    }));

    it('should NOT generate when GET fails with a non-404 error', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(serverError()));
      createHost();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
      expect(component.error).toBeTruthy();
      expect(component.explanation).toBeNull();
    }));
  });

  describe('display', () => {

    it('should display the summary', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('A clear summary')));
      createHost();
      tick();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).toContain('A clear summary');
    }));

    it('should display all experience explanations', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();
      tick();
      hostFixture.detectChanges();

      const text = hostFixture.nativeElement.textContent;
      expect(text).toContain('Directly relevant Java experience.');
      expect(text).toContain('Relevant leadership experience.');
    }));

    it('should display limitations when non-empty', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();
      tick();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).toContain('Limited detail in job description.');
    }));

    it('should NOT display a limitations section when empty', fakeAsync(() => {
      const explanation = explanationFixture('S');
      explanation.limitations = [];
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
      createHost();
      tick();
      hostFixture.detectChanges();

      const headings = Array.from(hostFixture.nativeElement.querySelectorAll('h6'))
        .map((el: HTMLElement) => el.textContent);
      expect(headings).not.toContain('Limitations');
    }));
  });

  describe('regeneration', () => {

    beforeEach(fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Original summary')));
      createHost();
      tick();
    }));

    it('should invoke POST generation even though an explanation already exists', () => {
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('Regenerated')));

      component.regenerate();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalled();
    });

    it('should replace the displayed explanation on successful regeneration', fakeAsync(() => {
      const regenerated = explanationFixture('Regenerated summary');
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(regenerated));

      component.regenerate();
      tick();

      expect(component.explanation).toEqual(regenerated);
    }));

    it('should retain the previous explanation when regeneration fails', fakeAsync(() => {
      const original = component.explanation;
      candidateServiceSpy.generateMatchExplanation.and.returnValue(throwError('LLM unavailable'));

      component.regenerate();
      tick();

      expect(component.explanation).toEqual(original);
      expect(component.error).toBeTruthy();
    }));

    it('should prevent a duplicate regenerate call while one is already in flight', () => {
      const inFlight = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.generateMatchExplanation.and.returnValue(inFlight);

      component.regenerate();
      component.regenerate();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalledTimes(1);
    });
  });

  describe('input changes', () => {

    it('should retrieve the explanation for a new candidateId/jobId pair', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('First')));
      createHost();
      tick();

      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Second')));
      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledWith(2, 200);
    }));

    it('should not let a stale response from the previous pair overwrite current state', fakeAsync(() => {
      const firstPairResponse = new Subject<CandidateMatchExplanation>();
      const secondPairResponse = new Subject<CandidateMatchExplanation>();

      candidateServiceSpy.getMatchExplanation.and.returnValue(firstPairResponse);
      createHost();

      // Switch to a new pair before the first pair's GET has resolved.
      candidateServiceSpy.getMatchExplanation.and.returnValue(secondPairResponse);
      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();

      // Second pair resolves first...
      secondPairResponse.next(explanationFixture('Second pair result'));
      tick();
      // ...then the stale first pair's response finally arrives.
      firstPairResponse.next(explanationFixture('Stale first pair result'));
      tick();

      expect(component.explanation.summary).toBe('Second pair result');
    }));

    it('should NOT trigger GET or POST when only opportunityDescription changes', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();
      tick();
      candidateServiceSpy.getMatchExplanation.calls.reset();
      candidateServiceSpy.generateMatchExplanation.calls.reset();

      hostComponent.opportunityDescription = 'A brand new description';
      hostFixture.detectChanges();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).not.toHaveBeenCalled();
      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should use the new opportunityDescription on a subsequent explicit regeneration', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();
      tick();

      hostComponent.opportunityDescription = 'A brand new description';
      hostFixture.detectChanges();

      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('Regenerated')));
      component.regenerate();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalledWith(1, {
        jobId: 100,
        opportunityDescription: 'A brand new description'
      });
    }));
  });
});
