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
import {ExtendDatePipe} from '../../../util/date-adapter/extend-date-pipe';

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
  jobId: number | undefined = 100;
  opportunityDescription: string | undefined = 'Initial opportunity description';
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
      declarations: [CandidateMatchExplanationComponent, TestHostComponent, ExtendDatePipe],
      providers: [
        {provide: CandidateService, useValue: spy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  // Note: `null` (not `undefined`) is the "absent" sentinel for these parameters, since a JS
  // default parameter value kicks in for an explicitly-passed `undefined` argument too, not just
  // an omitted one - `createHost(undefined)` would otherwise silently fall back to the default.
  function createHost(
    initialJobId: number | null = 100,
    initialDescription: string | null = 'Initial opportunity description'
  ) {
    hostFixture = TestBed.createComponent(TestHostComponent);
    hostComponent = hostFixture.componentInstance;
    hostComponent.jobId = initialJobId === null ? undefined : initialJobId;
    hostComponent.opportunityDescription = initialDescription === null ? undefined : initialDescription;
    hostFixture.detectChanges();
    component = hostFixture.debugElement
      .query(By.directive(CandidateMatchExplanationComponent)).componentInstance;
  }

  it('should receive candidateId, jobId and opportunityDescription from the host', () => {
    createHost();

    expect(component.candidateId).toBe(1);
    expect(component.jobId).toBe(100);
    expect(component.opportunityDescription).toBe('Initial opportunity description');
  });

  describe('passive initialization (no automatic network activity)', () => {

    it('should start closed', () => {
      createHost();

      expect(component.opened).toBeFalse();
    });

    it('should NOT call GET merely from instantiation', fakeAsync(() => {
      createHost();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should NOT call POST merely from instantiation', fakeAsync(() => {
      createHost();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should NOT call GET or POST when opportunityDescription is supplied but never opened', fakeAsync(() => {
      createHost(null, 'Some description');
      tick();

      expect(candidateServiceSpy.getMatchExplanation).not.toHaveBeenCalled();
      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));
  });

  describe('mode: jobId + opportunityDescription (opened via show())', () => {

    it('should retrieve an existing explanation via GET', fakeAsync(() => {
      const explanation = explanationFixture('Existing summary');
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
      createHost();

      component.show();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledWith(1, 100);
    }));

    it('should display the existing explanation and NOT call POST', fakeAsync(() => {
      const explanation = explanationFixture('Existing summary');
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
      createHost();

      component.show();
      tick();

      expect(component.explanation).toEqual(explanation);
      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should automatically generate when GET returns 404', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(notFoundError()));
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('Generated')));
      createHost();

      component.show();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalled();
      expect(component.explanation.summary).toBe('Generated');
    }));

    it('should generate using the current candidateId, jobId and opportunityDescription', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(notFoundError()));
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('G')));
      createHost();

      component.show();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalledWith(1, {
        jobId: 100,
        opportunityDescription: 'Initial opportunity description'
      });
    }));

    it('should NOT generate when GET fails with a non-404 error', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(serverError()));
      createHost();

      component.show();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
      expect(component.error).toBeTruthy();
      expect(component.explanation).toBeNull();
    }));

    it('should offer Regenerate once an explanation is displayed', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();

      component.show();
      tick();

      expect(component.hasUsableDescription()).toBeTrue();
    }));
  });

  describe('mode: opportunityDescription only (no jobId)', () => {

    it('should generate directly, without calling GET, when jobId is absent', fakeAsync(() => {
      candidateServiceSpy.generateMatchExplanation.and.returnValue(
        of(explanationFixture('Generated without a job')));
      createHost(null);

      component.show();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).not.toHaveBeenCalled();
      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalledWith(1, {
        jobId: undefined,
        opportunityDescription: 'Initial opportunity description'
      });
      expect(component.explanation.summary).toBe('Generated without a job');
    }));

    it('should surface an error when generation fails and jobId is absent', fakeAsync(() => {
      candidateServiceSpy.generateMatchExplanation.and.returnValue(throwError('LLM unavailable'));
      createHost(null);

      component.show();
      tick();

      expect(component.error).toBeTruthy();
      expect(component.explanation).toBeNull();
    }));

    it('should offer Regenerate once generated', fakeAsync(() => {
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('Generated')));
      createHost(null);

      component.show();
      tick();

      expect(component.hasUsableDescription()).toBeTrue();
    }));
  });

  describe('mode: jobId only (no opportunityDescription)', () => {

    it('should retrieve an existing explanation via GET', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Persisted')));
      createHost(100, null);

      component.show();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledWith(1, 100);
      expect(component.explanation.summary).toBe('Persisted');
    }));

    it('should NOT generate when GET returns 404, and should show the "no explanation" state', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(notFoundError()));
      createHost(100, null);

      component.show();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
      expect(component.explanation).toBeNull();
      expect(component.error).toBeNull();
      expect(component.loaded).toBeTrue();
    }));

    it('should NOT offer Regenerate even when a persisted explanation is displayed', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Persisted')));
      createHost(100, null);

      component.show();
      tick();

      expect(component.explanation).toBeTruthy();
      expect(component.hasUsableDescription()).toBeFalse();
    }));

    it('should not allow regenerate() to make a request without a usable description', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Persisted')));
      createHost(100, null);

      component.show();
      tick();
      component.regenerate();

      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));
  });

  describe('mode: neither jobId nor opportunityDescription', () => {

    it('should have no context and offer no "Show" action', () => {
      createHost(null, null);

      expect(component.hasContext()).toBeFalse();
    });

    it('should NOT call GET or POST even if show() were somehow invoked', fakeAsync(() => {
      createHost(null, null);

      component.show();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).not.toHaveBeenCalled();
      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));
  });

  describe('blank opportunityDescription', () => {

    it('should be treated the same as absent (no GET, no POST) for hasContext purposes when no jobId either', fakeAsync(() => {
      createHost(null, '   ');

      expect(component.hasContext()).toBeFalse();
    }));

    it('should not be usable for an explicit regenerate() call', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Persisted')));
      createHost(100, '   ');

      component.show();
      tick();
      component.regenerate();

      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));
  });

  describe('display', () => {

    it('should display the summary', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('A clear summary')));
      createHost();

      component.show();
      tick();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).toContain('A clear summary');
    }));

    it('should display all experience explanations', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();

      component.show();
      tick();
      hostFixture.detectChanges();

      const text = hostFixture.nativeElement.textContent;
      expect(text).toContain('Directly relevant Java experience.');
      expect(text).toContain('Relevant leadership experience.');
    }));

    it('should display limitations when non-empty', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();

      component.show();
      tick();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).toContain('Limited detail in job description.');
    }));

    it('should NOT display a limitations section when empty', fakeAsync(() => {
      const explanation = explanationFixture('S');
      explanation.limitations = [];
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
      createHost();

      component.show();
      tick();
      hostFixture.detectChanges();

      const headings = Array.from(hostFixture.nativeElement.querySelectorAll('h6'))
        .map((el: HTMLElement) => el.textContent);
      expect(headings).not.toContain('Limitations');
    }));

    describe('experience headings', () => {

      function headingTexts(): string[] {
        return Array.from(hostFixture.nativeElement.querySelectorAll('li strong'))
          .map((el: HTMLElement) => el.textContent.trim());
      }

      function showWith(explanation: CandidateMatchExplanation) {
        candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
        createHost();
        component.show();
        tick();
        hostFixture.detectChanges();
      }

      it('should render "<Job Title> (<id>)" when jobTitle is present', fakeAsync(() => {
        const explanation = explanationFixture('S');
        explanation.experienceExplanations[0].jobTitle = 'Senior Software Engineer';
        explanation.experienceExplanations[1].jobTitle = 'Team Lead';
        showWith(explanation);

        expect(headingTexts()).toEqual(['Senior Software Engineer (501):', 'Team Lead (502):']);
      }));

      it('should fall back to "Experience #<id>" when jobTitle is absent (e.g. legacy explanation)', fakeAsync(() => {
        showWith(explanationFixture('S'));

        expect(headingTexts()).toEqual(['Experience #501:', 'Experience #502:']);
      }));

      it('should fall back to "Experience #<id>" when jobTitle is null or blank, never rendering "null"/"undefined"', fakeAsync(() => {
        const explanation = explanationFixture('S');
        explanation.experienceExplanations[0].jobTitle = null;
        explanation.experienceExplanations[1].jobTitle = '  ';
        showWith(explanation);

        expect(headingTexts()).toEqual(['Experience #501:', 'Experience #502:']);
        const text = hostFixture.nativeElement.textContent;
        expect(text).not.toContain('null');
        expect(text).not.toContain('undefined');
      }));
    });

    describe('generated-at / model metadata', () => {

      const GENERATED_AT = '2026-10-02T03:30:00Z';
      const MODEL_NAME = 'qwen.qwen3-235b-a22b-2507-v1:0';

      function metadataLine(): HTMLElement | null {
        return hostFixture.nativeElement.querySelector('.match-explanation-metadata');
      }

      function showWith(explanation: CandidateMatchExplanation) {
        candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanation));
        createHost();
        component.show();
        tick();
        hostFixture.detectChanges();
      }

      // Formatted with the same pipe/format as the template, so the expectation follows the
      // test runner's local timezone just as the rendered value does.
      function formattedGeneratedAt(): string {
        return new ExtendDatePipe('en-US').transform(GENERATED_AT, 'customDateTime');
      }

      it('should display both generated timestamp and model name when present', fakeAsync(() => {
        showWith({...explanationFixture('S'), generatedAt: GENERATED_AT, modelName: MODEL_NAME});

        const text = metadataLine().textContent.replace(/\s+/g, ' ').trim();
        expect(text).toBe(`Generated ${formattedGeneratedAt()} · ${MODEL_NAME}`);
      }));

      it('should display only the generated timestamp when modelName is missing', fakeAsync(() => {
        showWith({...explanationFixture('S'), generatedAt: GENERATED_AT, modelName: null});

        const text = metadataLine().textContent.replace(/\s+/g, ' ').trim();
        expect(text).toBe(`Generated ${formattedGeneratedAt()}`);
        expect(text).not.toContain('·');
      }));

      it('should display only the model name when generatedAt is missing', fakeAsync(() => {
        showWith({...explanationFixture('S'), modelName: MODEL_NAME});

        const text = metadataLine().textContent.replace(/\s+/g, ' ').trim();
        expect(text).toBe(MODEL_NAME);
        expect(text).not.toContain('Generated');
        expect(text).not.toContain('·');
      }));

      it('should render no metadata line when both are missing (e.g. legacy explanation)', fakeAsync(() => {
        showWith({...explanationFixture('Legacy summary'), generatedAt: null, modelName: null});

        expect(metadataLine()).toBeNull();
        expect(hostFixture.nativeElement.textContent).toContain('Legacy summary');
        expect(hostFixture.nativeElement.textContent).not.toContain('Generated');
      }));
    });

    it('should not display anything when closed', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Hidden summary')));
      createHost();

      component.show();
      tick();
      component.close();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).not.toContain('Hidden summary');
    }));
  });

  describe('closing and reopening', () => {

    it('should redisplay an already-loaded explanation without a new GET when reopened', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Loaded once')));
      createHost();

      component.show();
      tick();
      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledTimes(1);

      component.close();
      expect(component.explanation.summary).toBe('Loaded once');

      component.show();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledTimes(1);
      expect(component.explanation.summary).toBe('Loaded once');
    }));

    it('should redisplay an already-generated explanation without a new POST when reopened', fakeAsync(() => {
      candidateServiceSpy.generateMatchExplanation.and.returnValue(of(explanationFixture('Generated once')));
      createHost(null);

      component.show();
      tick();
      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalledTimes(1);

      component.close();
      component.show();
      tick();

      expect(candidateServiceSpy.generateMatchExplanation).toHaveBeenCalledTimes(1);
      expect(component.explanation.summary).toBe('Generated once');
    }));

    it('should redisplay the "no explanation" state without a new GET when reopened', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(throwError(notFoundError()));
      createHost(100, null);

      component.show();
      tick();
      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledTimes(1);

      component.close();
      component.show();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledTimes(1);
      expect(component.explanation).toBeNull();
    }));
  });

  describe('regeneration', () => {

    beforeEach(fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Original summary')));
      createHost();
      component.show();
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

    it('should NOT automatically retrieve/generate for a new candidateId/jobId pair', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('First')));
      createHost();
      component.show();
      tick();
      candidateServiceSpy.getMatchExplanation.calls.reset();

      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should clear the previous explanation and return to the closed state on candidateId/jobId change', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('First')));
      createHost();
      component.show();
      tick();
      expect(component.opened).toBeTrue();
      expect(component.explanation).toBeTruthy();

      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();
      tick();

      expect(component.opened).toBeFalse();
      expect(component.explanation).toBeNull();
      expect(component.loaded).toBeFalse();
    }));

    it('should require an explicit show() to retrieve for the new pair', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('First')));
      createHost();
      component.show();
      tick();

      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('Second')));
      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();
      tick();

      component.show();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).toHaveBeenCalledWith(2, 200);
      expect(component.explanation.summary).toBe('Second');
    }));

    it('should not let a stale response from the previous pair overwrite current state', fakeAsync(() => {
      const firstPairResponse = new Subject<CandidateMatchExplanation>();
      const secondPairResponse = new Subject<CandidateMatchExplanation>();

      candidateServiceSpy.getMatchExplanation.and.returnValue(firstPairResponse);
      createHost();
      component.show(); // pair A's GET in flight

      // Switch to a new pair before the first pair's GET has resolved - this cancels A's GET and
      // returns to the closed state.
      candidateServiceSpy.getMatchExplanation.and.returnValue(secondPairResponse);
      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();

      component.show(); // pair B's GET in flight

      // Second pair resolves first...
      secondPairResponse.next(explanationFixture('Second pair result'));
      tick();
      // ...then the stale first pair's response finally arrives (already unsubscribed - no-op).
      firstPairResponse.next(explanationFixture('Stale first pair result'));
      tick();

      expect(component.explanation.summary).toBe('Second pair result');
    }));

    it('should NOT trigger GET or POST when only opportunityDescription changes (not yet opened)', fakeAsync(() => {
      createHost();

      hostComponent.opportunityDescription = 'A brand new description';
      hostFixture.detectChanges();
      tick();

      expect(candidateServiceSpy.getMatchExplanation).not.toHaveBeenCalled();
      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should retain a displayed explanation when only opportunityDescription changes', fakeAsync(() => {
      const persisted = explanationFixture('Persisted summary');
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(persisted));
      createHost();
      component.show();
      tick();

      hostComponent.opportunityDescription = 'A brand new description';
      hostFixture.detectChanges();
      tick();

      expect(component.explanation).toEqual(persisted);
      expect(candidateServiceSpy.generateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should use the new opportunityDescription on a subsequent explicit regeneration', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();
      component.show();
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

  describe('stale explicit regeneration', () => {

    it('should not let a stale (previous-pair) regeneration success affect the current pair', fakeAsync(() => {
      // Establish pair A (candidateId=1, jobId=100) with its initial explanation.
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('A original')));
      createHost();
      component.show();
      tick();

      // 1. Start regeneration for pair A and leave it in flight.
      const regenerateA = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.generateMatchExplanation.and.returnValue(regenerateA);
      component.regenerate();
      expect(component.regenerating).toBeTrue();

      // 2. Change inputs to pair B - closes/resets, cancelling A's in-flight regenerate.
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('B original')));
      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();
      expect(component.regenerating).toBeFalse();

      // 3. Open and establish B's explanation.
      component.show();
      tick();
      expect(component.explanation.summary).toBe('B original');

      // 4. Start regeneration for B and leave it in flight.
      const regenerateB = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.generateMatchExplanation.and.returnValue(regenerateB);
      component.regenerate();
      expect(component.regenerating).toBeTrue();

      // 5. Complete A's old (stale) regeneration.
      regenerateA.next(explanationFixture('A stale result'));
      tick();

      // 6. B must still be regenerating, with its explanation/error untouched by A's stale result.
      expect(component.regenerating).toBeTrue();
      expect(component.explanation.summary).toBe('B original');
      expect(component.error).toBeNull();

      // 7. Complete B's regeneration.
      regenerateB.next(explanationFixture('B regenerated'));
      tick();

      // 8. Regenerating clears and B's new explanation is displayed.
      expect(component.regenerating).toBeFalse();
      expect(component.explanation.summary).toBe('B regenerated');
    }));

    it('should not let a stale (previous-pair) regeneration error affect the current pair', fakeAsync(() => {
      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('A original')));
      createHost();
      component.show();
      tick();

      const regenerateA = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.generateMatchExplanation.and.returnValue(regenerateA);
      component.regenerate();

      candidateServiceSpy.getMatchExplanation.and.returnValue(of(explanationFixture('B original')));
      hostComponent.candidateId = 2;
      hostComponent.jobId = 200;
      hostFixture.detectChanges();

      component.show();
      tick();

      const regenerateB = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.generateMatchExplanation.and.returnValue(regenerateB);
      component.regenerate();
      expect(component.regenerating).toBeTrue();

      // A's stale regeneration fails - must not clear B's regenerating state, set B's error, or
      // change B's displayed explanation.
      regenerateA.error('A stale error');
      tick();

      expect(component.regenerating).toBeTrue();
      expect(component.error).toBeNull();
      expect(component.explanation.summary).toBe('B original');

      // B's own regeneration still completes normally afterwards.
      regenerateB.next(explanationFixture('B regenerated'));
      tick();

      expect(component.regenerating).toBeFalse();
      expect(component.explanation.summary).toBe('B regenerated');
    }));
  });
});
