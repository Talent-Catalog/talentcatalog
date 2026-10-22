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


import {Component, NO_ERRORS_SCHEMA, ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {Subject, of, throwError} from 'rxjs';
import {CandidateMatchExplanationComponent} from './candidate-match-explanation.component';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateMatchExplanation} from '../../../model/candidate-match-explanation';
import {ExtendDatePipe} from '../../../util/date-adapter/extend-date-pipe';
import {NgbConfig, NgbPopoverModule} from '@ng-bootstrap/ng-bootstrap';
import {
  CandidateJobExperienceComponent
} from '../../candidates/view/occupation/experience/candidate-job-experience/candidate-job-experience.component';
import {Candidate} from '../../../model/candidate';
import {CandidateJobExperience} from '../../../model/candidate-job-experience';

@Component({
  template: `
    <app-candidate-match-explanation
      [candidateId]="candidateId"
      [candidate]="candidate"
      [matchingId]="matchingId"
      [matchingUpdatedDate]="matchingUpdatedDate"
      [requirementsPendingMessage]="requirementsPendingMessage">
    </app-candidate-match-explanation>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
class TestHostComponent {
  candidateId = 1;
  candidate: Candidate | undefined = undefined;
  matchingId: number | undefined = 5;
  matchingUpdatedDate: string | undefined = '2026-10-01T10:15:30.123456Z';
  requirementsPendingMessage: string | undefined = undefined;
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
    const spy = jasmine.createSpyObj('CandidateService', ['getOrGenerateMatchExplanation']);

    await TestBed.configureTestingModule({
      declarations: [
        CandidateMatchExplanationComponent, TestHostComponent, ExtendDatePipe,
        CandidateJobExperienceComponent
      ],
      imports: [NgbPopoverModule],
      providers: [
        {provide: CandidateService, useValue: spy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    // Popovers open/close synchronously without animation, so fakeAsync tests aren't left with
    // pending transition timers.
    TestBed.inject(NgbConfig).animation = false;
  });

  // Note: `null` (not `undefined`) is the "absent" sentinel for matchingId, since a JS default
  // parameter value kicks in for an explicitly-passed `undefined` argument too.
  function createHost(initialMatchingId: number | null = 5, candidate?: Candidate) {
    hostFixture = TestBed.createComponent(TestHostComponent);
    hostComponent = hostFixture.componentInstance;
    hostComponent.candidate = candidate;
    hostComponent.matchingId = initialMatchingId === null ? undefined : initialMatchingId;
    hostFixture.detectChanges();
    component = hostFixture.debugElement
      .query(By.directive(CandidateMatchExplanationComponent)).componentInstance;
  }

  function text(): string {
    return hostFixture.nativeElement.textContent;
  }

  function showAndRender() {
    component.show();
    tick();
    hostFixture.detectChanges();
  }

  it('should receive candidateId, matchingId and matchingUpdatedDate from the host', () => {
    createHost();

    expect(component.candidateId).toBe(1);
    expect(component.matchingId).toBe(5);
    expect(component.matchingUpdatedDate).toBe('2026-10-01T10:15:30.123456Z');
  });

  describe('passive initialization (no automatic network activity)', () => {

    it('should start closed, offering "Show match explanation"', () => {
      createHost();

      expect(component.opened).toBeFalse();
      expect(text()).toContain('Show match explanation');
    });

    it('should NOT request an explanation merely from instantiation', fakeAsync(() => {
      createHost();
      tick();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should offer nothing without a Matching', () => {
      createHost(null);

      expect(text()).not.toContain('Show match explanation');
    });
  });

  describe('showing', () => {

    it('should get the explanation for the candidate and Matching, without regenerating', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();

      showAndRender();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation)
        .toHaveBeenCalledOnceWith(1, {matchingId: 5, regenerate: false});
      expect(text()).toContain('Directly relevant Java experience.');
    }));

    it('should show a loading state while the explanation is being fetched or generated', fakeAsync(() => {
      const response = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(response);
      createHost();

      showAndRender();
      expect(component.loading).toBeTrue();
      expect(text()).toContain('Getting match explanation...');

      response.next(explanationFixture('Arrived'));
      tick();
      hostFixture.detectChanges();
      expect(component.loading).toBeFalse();
      expect(text()).not.toContain('Getting match explanation...');
      expect(text()).toContain('Arrived');
    }));

    it('should show the error, and offer to try again, when the request fails', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(
        throwError('Matching 5 has no description'));
      createHost();

      showAndRender();

      expect(component.error).toBe('Matching 5 has no description');
      expect(text()).toContain('Matching 5 has no description');
      expect(text()).toContain('Try again');
      expect(component.explanation).toBeNull();
    }));

    it('should get the explanation again when retrying after a failure', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValues(
        throwError('Service unavailable'), of(explanationFixture('Second try')));
      createHost();
      showAndRender();

      component.retry();
      tick();
      hostFixture.detectChanges();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation).toHaveBeenCalledTimes(2);
      expect(component.error).toBeNull();
      expect(text()).toContain('Second try');
      expect(text()).not.toContain('Try again');
    }));

    it('should not make a second request while one is in flight', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(new Subject());
      createHost();

      component.show();
      component.close();
      component.show();
      component.retry();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation).toHaveBeenCalledTimes(1);
    }));
  });

  describe('requirements pending', () => {

    it('should show the message instead of the explanation UI, making no request', fakeAsync(() => {
      createHost();
      hostComponent.requirementsPendingMessage = 'Save the search to see match explanations';
      hostFixture.detectChanges();

      expect(text()).toContain('Save the search to see match explanations');
      expect(text()).not.toContain('Show match explanation');
      component.show();
      tick();
      expect(candidateServiceSpy.getOrGenerateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should show the message even when there is no Matching yet', () => {
      createHost(null);
      hostComponent.requirementsPendingMessage = 'Run the search';
      hostFixture.detectChanges();

      expect(text()).toContain('Run the search');
    });
  });

  describe('display', () => {

    it('should display the summary', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('A clear summary')));
      createHost();

      component.show();
      tick();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).toContain('A clear summary');
    }));

    it('should display all experience explanations', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();

      component.show();
      tick();
      hostFixture.detectChanges();

      const text = hostFixture.nativeElement.textContent;
      expect(text).toContain('Directly relevant Java experience.');
      expect(text).toContain('Relevant leadership experience.');
    }));

    it('should display limitations when non-empty', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('S')));
      createHost();

      component.show();
      tick();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).toContain('Limited detail in job description.');
    }));

    it('should NOT display a limitations section when empty', fakeAsync(() => {
      const explanation = explanationFixture('S');
      explanation.limitations = [];
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanation));
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
        candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanation));
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
        candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanation));
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
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('Hidden summary')));
      createHost();

      component.show();
      tick();
      component.close();
      hostFixture.detectChanges();

      expect(hostFixture.nativeElement.textContent).not.toContain('Hidden summary');
    }));
  });

  describe('experience hover (current candidate job experience)', () => {

    function jobExperience(id: number, role: string): CandidateJobExperience {
      return {
        id,
        role,
        companyName: `Company ${id}`,
        country: {id: 1, name: 'Australia', status: 'active', translatedName: null},
        startDate: '2020-01-01',
        endDate: '2021-01-01',
        fullTime: true,
        paid: true,
        description: `Current description for ${id}`
      };
    }

    // Experiences deliberately listed in a different order from the explanation (501, 502), and
    // with current roles that differ from the explanation's persisted job titles.
    function candidateWithExperiences(): Candidate {
      return {
        id: 1,
        candidateJobExperiences: [jobExperience(502, 'Current Team Lead role')],
        candidateOccupations: [
          {candidateJobExperiences: [jobExperience(999, 'Unrelated role'), jobExperience(501, 'Current Developer role')]}
        ]
      } as any as Candidate;
    }

    function explanationWithTitles(): CandidateMatchExplanation {
      const explanation = explanationFixture('S');
      explanation.experienceExplanations[0].jobTitle = 'Historical Developer Title';
      explanation.experienceExplanations[1].jobTitle = 'Historical Lead Title';
      return explanation;
    }

    function showWith(explanation: CandidateMatchExplanation, candidate?: Candidate) {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanation));
      createHost(5, candidate);
      component.show();
      tick();
      hostFixture.detectChanges();
    }

    function headings(): HTMLElement[] {
      return Array.from(hostFixture.nativeElement.querySelectorAll('li strong'));
    }

    function popoverElement(): HTMLElement | null {
      return document.body.querySelector('ngb-popover-window');
    }

    afterEach(() => {
      // Popovers are attached to the body, so make sure none leak between tests.
      hostFixture?.destroy();
    });

    describe('hover interaction', () => {
      const BRIDGE = CandidateMatchExplanationComponent.EXPERIENCE_POPOVER_BRIDGE_MS;

      function popoverContent(): HTMLElement | null {
        return document.body.querySelector('.candidate-job-experience-popover-content');
      }

      function fire(el: Element, type: string) {
        el.dispatchEvent(new Event(type));
        hostFixture.detectChanges();
      }

      /** Dispatches a focus event, controlling whether it counts as keyboard (:focus-visible) focus. */
      function focus(el: HTMLElement, keyboard: boolean) {
        const matches = el.matches.bind(el);
        spyOn(el, 'matches').and.callFake(
          ((selector: string) => selector === ':focus-visible' ? keyboard : matches(selector)) as any);
        el.dispatchEvent(new FocusEvent('focus'));
        hostFixture.detectChanges();
      }

      function openFirst(): HTMLElement {
        showWith(explanationWithTitles(), candidateWithExperiences());
        const heading = headings()[0];
        fire(heading, 'mouseenter');
        return heading;
      }

      it('should open the popover when the pointer enters the heading', fakeAsync(() => {
        openFirst();

        expect(popoverElement()).not.toBeNull();
        expect(popoverContent().querySelector('app-candidate-job-experience')).not.toBeNull();
      }));

      it('should stay open when the pointer moves from the heading into the popover', fakeAsync(() => {
        const heading = openFirst();

        fire(heading, 'mouseleave');
        tick(BRIDGE / 2);
        fire(popoverContent(), 'mouseenter');
        tick(BRIDGE * 2);
        hostFixture.detectChanges();

        expect(popoverElement()).not.toBeNull();
      }));

      it('should remain open, however long, while the pointer is over the popover (to scroll/select)', fakeAsync(() => {
        const heading = openFirst();
        fire(heading, 'mouseleave');
        fire(popoverContent(), 'mouseenter');

        tick(10000);
        fire(popoverContent(), 'wheel');
        fire(popoverContent(), 'scroll');
        popoverContent().click();
        tick(10000);
        hostFixture.detectChanges();

        expect(popoverElement()).not.toBeNull();
      }));

      it('should make the content wrapper the scroll container, so its scrollbar is inside the hover area', fakeAsync(() => {
        openFirst();

        expect(getComputedStyle(popoverContent()).overflowY).toBe('auto');
        expect(getComputedStyle(popoverElement().querySelector('.popover-body')).paddingTop).toBe('0px');
      }));

      it('should close when the pointer leaves the heading without entering the popover', fakeAsync(() => {
        const heading = openFirst();

        fire(heading, 'mouseleave');
        tick(BRIDGE);
        hostFixture.detectChanges();

        expect(popoverElement()).toBeNull();
      }));

      it('should close when the pointer leaves the popover', fakeAsync(() => {
        const heading = openFirst();
        fire(heading, 'mouseleave');
        fire(popoverContent(), 'mouseenter');

        fire(popoverContent(), 'mouseleave');
        tick(BRIDGE);
        hostFixture.detectChanges();

        expect(popoverElement()).toBeNull();
      }));

      it('should stay open when the pointer moves from the popover back to the heading', fakeAsync(() => {
        const heading = openFirst();
        fire(heading, 'mouseleave');
        fire(popoverContent(), 'mouseenter');

        fire(popoverContent(), 'mouseleave');
        fire(heading, 'mouseenter');
        tick(BRIDGE * 2);
        hostFixture.detectChanges();

        expect(popoverElement()).not.toBeNull();
      }));

      it('should show only one experience popover at a time', fakeAsync(() => {
        const first = openFirst();
        fire(first, 'mouseleave');
        fire(headings()[1], 'mouseenter');

        expect(document.body.querySelectorAll('ngb-popover-window').length).toBe(1);
        expect(popoverElement().textContent).toContain('Current Team Lead role');
        tick(BRIDGE * 2);
        hostFixture.detectChanges();
        expect(document.body.querySelectorAll('ngb-popover-window').length).toBe(1);
      }));

      it('should close on Escape and not get stuck afterwards', fakeAsync(() => {
        const heading = openFirst();
        tick(); // ngbPopover registers its Escape/outside-click listeners asynchronously

        // ngbPopover's autoClose matches on the legacy `which` key code, which synthetic events lack
        const escape = new KeyboardEvent('keydown', {key: 'Escape'});
        Object.defineProperty(escape, 'which', {get: () => 27});
        document.dispatchEvent(escape);
        hostFixture.detectChanges();
        expect(popoverElement()).toBeNull();

        fire(heading, 'mouseleave');
        tick(BRIDGE);
        fire(heading, 'mouseenter');
        expect(popoverElement()).not.toBeNull();
        fire(heading, 'mouseleave');
        tick(BRIDGE);
        hostFixture.detectChanges();
        expect(popoverElement()).toBeNull();
      }));

      it('should open on keyboard focus and close on blur', fakeAsync(() => {
        showWith(explanationWithTitles(), candidateWithExperiences());
        const heading = headings()[0];

        expect(heading.getAttribute('tabindex')).toBe('0');
        focus(heading, true);
        expect(popoverElement()).not.toBeNull();
        tick(BRIDGE * 2);
        expect(popoverElement()).not.toBeNull();

        fire(heading, 'blur');
        tick(BRIDGE);
        hostFixture.detectChanges();
        expect(popoverElement()).toBeNull();
      }));

      it('should not be held open by focus from a mouse click once the pointer leaves', fakeAsync(() => {
        const heading = openFirst();
        focus(heading, false);

        fire(heading, 'mouseleave');
        tick(BRIDGE);
        hostFixture.detectChanges();

        expect(popoverElement()).toBeNull();
      }));

      it('should close any open popover, without errors, when the component is destroyed mid-bridge', fakeAsync(() => {
        const heading = openFirst();
        fire(heading, 'mouseleave');

        hostFixture.destroy();
        tick(BRIDGE);

        expect(popoverElement()).toBeNull();
      }));
    });

    it('should receive the candidate from the host', fakeAsync(() => {
      const candidate = candidateWithExperiences();
      showWith(explanationWithTitles(), candidate);

      expect(component.candidate).toBe(candidate);
    }));

    it('should resolve experiences by ID, from top-level or occupation experiences, regardless of position', fakeAsync(() => {
      showWith(explanationWithTitles(), candidateWithExperiences());

      expect(component.findExperience(501).role).toBe('Current Developer role');
      expect(component.findExperience(502).role).toBe('Current Team Lead role');
      expect(component.findExperience(999).role).toBe('Unrelated role');
      expect(component.findExperience(12345)).toBeUndefined();
    }));

    it('should tolerate occupations without any job experiences', fakeAsync(() => {
      const candidate = {
        id: 1,
        candidateOccupations: [
          {candidateJobExperiences: undefined},
          {candidateJobExperiences: [jobExperience(501, 'Current Developer role')]}
        ]
      } as any as Candidate;
      showWith(explanationWithTitles(), candidate);

      expect(component.findExperience(501).role).toBe('Current Developer role');
      expect(component.findExperience(502)).toBeUndefined();
    }));

    it('should re-index experiences when the candidate input changes', fakeAsync(() => {
      showWith(explanationWithTitles(), candidateWithExperiences());

      hostComponent.candidate = {id: 1, candidateJobExperiences: [jobExperience(501, 'Edited role')]} as any;
      hostFixture.detectChanges();

      expect(component.findExperience(501).role).toBe('Edited role');
      expect(component.findExperience(502)).toBeUndefined();
      // Updating the candidate data does not disturb the displayed explanation
      expect(component.explanation).not.toBeNull();
    }));

    it('should make headings hover targets when the experience exists', fakeAsync(() => {
      showWith(explanationWithTitles(), candidateWithExperiences());

      const hoverTargets = hostFixture.nativeElement.querySelectorAll('li strong.experience-heading-hover');
      expect(hoverTargets.length).toBe(2);
    }));

    it('should still head explanations with the persisted jobTitle, not the current role', fakeAsync(() => {
      showWith(explanationWithTitles(), candidateWithExperiences());

      expect(headings().map(h => h.textContent.trim()))
        .toEqual(['Historical Developer Title (501):', 'Historical Lead Title (502):']);
    }));

    it('should keep the "Experience #<id>" fallback heading when jobTitle is absent', fakeAsync(() => {
      showWith(explanationFixture('S'), candidateWithExperiences());

      expect(headings().map(h => h.textContent.trim()))
        .toEqual(['Experience #501:', 'Experience #502:']);
      expect(hostFixture.nativeElement.querySelectorAll('strong.experience-heading-hover').length).toBe(2);
    }));

    it('should show the matching current experience via the reusable experience component on hover', fakeAsync(() => {
      showWith(explanationWithTitles(), candidateWithExperiences());

      // Hover the second heading (502), whose experience is first in the candidate's top-level list
      headings()[1].dispatchEvent(new Event('mouseenter'));
      hostFixture.detectChanges();

      const popover = popoverElement();
      expect(popover).not.toBeNull();
      expect(popover.querySelector('app-candidate-job-experience')).not.toBeNull();
      expect(popover.textContent).toContain('Current Team Lead role');
      expect(popover.textContent).toContain('Company 502, Australia');
      expect(popover.textContent).not.toContain('Current Developer role');

      headings()[1].dispatchEvent(new Event('mouseleave'));
      tick(CandidateMatchExplanationComponent.EXPERIENCE_POPOVER_BRIDGE_MS);
      hostFixture.detectChanges();
      expect(popoverElement()).toBeNull();
    }));

    it('should handle an experience that no longer exists: heading shown, no hover target or popover, no error', fakeAsync(() => {
      const candidate = candidateWithExperiences();
      // 502 has since been deleted from the candidate
      candidate.candidateJobExperiences = [];
      showWith(explanationWithTitles(), candidate);

      const [present, missing] = headings();
      expect(missing.textContent.trim()).toBe('Historical Lead Title (502):');
      expect(missing.classList).not.toContain('experience-heading-hover');
      expect(present.classList).toContain('experience-heading-hover');

      expect(() => {
        missing.dispatchEvent(new Event('mouseenter'));
        hostFixture.detectChanges();
      }).not.toThrow();
      expect(popoverElement()).toBeNull();
      expect(hostFixture.nativeElement.textContent).toContain('Relevant leadership experience.');
    }));

    it('should show plain headings with no popover when no candidate is supplied', fakeAsync(() => {
      showWith(explanationWithTitles());

      expect(hostFixture.nativeElement.querySelectorAll('strong.experience-heading-hover').length).toBe(0);
      expect(headings().length).toBe(2);
      headings()[0].dispatchEvent(new Event('mouseenter'));
      hostFixture.detectChanges();
      expect(popoverElement()).toBeNull();
    }));

    it('should make no additional candidate API requests to resolve or display experiences', fakeAsync(() => {
      showWith(explanationWithTitles(), candidateWithExperiences());

      headings()[0].dispatchEvent(new Event('mouseenter'));
      hostFixture.detectChanges();
      headings()[0].dispatchEvent(new Event('mouseleave'));
      tick(CandidateMatchExplanationComponent.EXPERIENCE_POPOVER_BRIDGE_MS);
      hostFixture.detectChanges();

      // The CandidateService spy only provides the explanation methods - any other call (e.g.
      // re-fetching the candidate) would throw. The explanation itself was requested just once.
      expect(candidateServiceSpy.getOrGenerateMatchExplanation).toHaveBeenCalledTimes(1);
    }));
  });


  describe('closing and reopening', () => {

    it('should redisplay an already-loaded explanation without a new request when reopened', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('Loaded')));
      createHost();
      showAndRender();

      component.close();
      hostFixture.detectChanges();
      expect(text()).not.toContain('Loaded');
      showAndRender();

      expect(text()).toContain('Loaded');
      expect(candidateServiceSpy.getOrGenerateMatchExplanation).toHaveBeenCalledTimes(1);
    }));
  });

  describe('regeneration', () => {

    beforeEach(fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('Original')));
      createHost();
      showAndRender();
      candidateServiceSpy.getOrGenerateMatchExplanation.calls.reset();
    }));

    it('should offer Regenerate once an explanation is displayed', () => {
      expect(text()).toContain('Regenerate explanation');
    });

    it('should request regeneration, bypassing the cache', () => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(new Subject());

      component.regenerate();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation)
        .toHaveBeenCalledOnceWith(1, {matchingId: 5, regenerate: true});
      expect(component.regenerating).toBeTrue();
      expect(component.loading).toBeFalse();
    });

    it('should keep displaying the current explanation while regenerating', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(new Subject());

      component.regenerate();
      hostFixture.detectChanges();

      expect(text()).toContain('Original');
    }));

    it('should replace the displayed explanation on successful regeneration', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('Regenerated')));

      component.regenerate();
      tick();
      hostFixture.detectChanges();

      expect(component.regenerating).toBeFalse();
      expect(text()).toContain('Regenerated');
      expect(text()).not.toContain('Original');
    }));

    it('should retain the previous explanation, showing the error, when regeneration fails', fakeAsync(() => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(throwError('Generation failed'));

      component.regenerate();
      tick();
      hostFixture.detectChanges();

      expect(text()).toContain('Original');
      expect(text()).toContain('Generation failed');
      expect(text()).not.toContain('Try again');
    }));

    it('should prevent a duplicate regenerate call while one is already in flight', () => {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(new Subject());

      component.regenerate();
      component.regenerate();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation).toHaveBeenCalledTimes(1);
    });
  });

  describe('input changes', () => {

    function showOriginal() {
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('Original')));
      createHost();
      showAndRender();
    }

    for (const [input, change] of [
      ['candidateId', (host: TestHostComponent) => host.candidateId = 2],
      ['matchingId', (host: TestHostComponent) => host.matchingId = 6],
      ['matchingUpdatedDate', (host: TestHostComponent) => host.matchingUpdatedDate = '2026-10-01T11:00:00Z']
    ] as [string, (host: TestHostComponent) => void][]) {

      it(`should discard the explanation and return to the closed state when ${input} changes`, fakeAsync(() => {
        showOriginal();

        change(hostComponent);
        hostFixture.detectChanges();

        expect(component.opened).toBeFalse();
        expect(component.explanation).toBeNull();
        expect(text()).not.toContain('Original');
        expect(text()).toContain('Show match explanation');
      }));
    }

    it('should NOT automatically request an explanation for the new context', fakeAsync(() => {
      showOriginal();
      candidateServiceSpy.getOrGenerateMatchExplanation.calls.reset();

      hostComponent.matchingUpdatedDate = '2026-10-01T11:00:00Z';
      hostFixture.detectChanges();
      tick();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation).not.toHaveBeenCalled();
    }));

    it('should request the new context\'s explanation when shown again', fakeAsync(() => {
      showOriginal();
      hostComponent.matchingId = 6;
      hostFixture.detectChanges();
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(of(explanationFixture('New')));

      showAndRender();

      expect(candidateServiceSpy.getOrGenerateMatchExplanation)
        .toHaveBeenCalledWith(1, {matchingId: 6, regenerate: false});
      expect(text()).toContain('New');
    }));

    it('should not let a response for the previous context be displayed', fakeAsync(() => {
      const stale = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(stale);
      createHost();
      component.show();

      hostComponent.matchingUpdatedDate = '2026-10-01T11:00:00Z';
      hostFixture.detectChanges();
      stale.next(explanationFixture('Stale'));
      tick();
      hostFixture.detectChanges();

      expect(component.explanation).toBeNull();
      expect(component.loading).toBeFalse();
      expect(text()).not.toContain('Stale');
    }));

    it('should not let a stale regeneration error affect the new context', fakeAsync(() => {
      showOriginal();
      const stale = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(stale);
      component.regenerate();

      hostComponent.candidateId = 2;
      hostFixture.detectChanges();
      stale.error('Stale failure');
      tick();

      expect(component.error).toBeNull();
      expect(component.regenerating).toBeFalse();
    }));

    it('should keep a displayed explanation when only the candidate data changes', fakeAsync(() => {
      showOriginal();

      hostComponent.candidate = {id: 1, candidateJobExperiences: []} as any;
      hostFixture.detectChanges();

      expect(text()).toContain('Original');
    }));
  });

  describe('destroy', () => {

    it('should unsubscribe from an in-flight request when destroyed', fakeAsync(() => {
      const response = new Subject<CandidateMatchExplanation>();
      candidateServiceSpy.getOrGenerateMatchExplanation.and.returnValue(response);
      createHost();
      component.show();

      hostFixture.destroy();

      expect(response.observers.length).toBe(0);
    }));
  });
});
