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
import {CandidateSearchCardComponent} from "./candidate-search-card.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {By} from "@angular/platform-browser";
import {AuthorizationService} from "../../../services/authorization.service";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {CandidateSource} from "../../../model/base";
import {Candidate} from "../../../model/candidate";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {LocalStorageService} from "../../../services/local-storage.service";
import {CandidateService} from "../../../services/candidate.service";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";

describe('CandidateSearchCardComponent', () => {
  let component: CandidateSearchCardComponent;
  let fixture: ComponentFixture<CandidateSearchCardComponent>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const localStorageSpy = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', [
      'canViewPrivateCandidateInfo', 'canSeeJobDetails', 'isAnAdmin', 'isEditableCandidate'
    ]);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidate']);

    await TestBed.configureTestingModule({
      declarations: [CandidateSearchCardComponent],
      providers: [
        { provide: LocalStorageService, useValue: localStorageSpy },
        { provide: AuthorizationService, useValue: authSpy },
        { provide: CandidateService, userValue: candidateServiceSpy },
        provideHttpClient(withXhr(), withInterceptorsFromDi()),
        provideHttpClientTesting()
      ],
      imports: [NgbNavModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    localStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateSearchCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle notes visibility', () => {
    component.showNotes = false;
    component.toggleNotes();
    expect(component.showNotes).toBeTrue();
  });

  it('should emit close event', () => {
    spyOn(component.closeEvent, 'emit');
    component.close();
    expect(component.closeEvent.emit).toHaveBeenCalled();
  });

  it('should set active tab ID and store in local storage', () => {
    component['setActiveTabId']('education');
    expect(localStorageService.set).toHaveBeenCalledWith(component['lastTabKey'], 'education');
  });

  it('should set active context tab ID and store in local storage', () => {
    component['setActiveContextTabId']('docs');
    expect(localStorageService.set).toHaveBeenCalledWith(component['lastContextTabKey'], 'docs');
  });

  it('should determine if context note is displayed', () => {
    const mockSavedList = new MockSavedSearch();
    mockSavedList.defaultSearch = false;
    component.candidateSource = mockSavedList;
    expect(component.isContextNoteDisplayed()).toBeTrue();

    mockSavedList.defaultSearch = true;
    component.candidateSource = mockSavedList;
    expect(component.isContextNoteDisplayed()).toBeFalse();
  });

  it('should determine if submission list', () => {
    component.candidateSource = { sfJobOpp: {} } as CandidateSource;
    spyOnProperty(component, 'isList', 'get').and.returnValue(true);
    expect(component.isSubmissionList()).toBeTrue();
  });

  it('should get candidate opportunity for job source', () => {
    const jobOpp = { id: 1 };
    const candidateOpportunities = [mockCandidateOpportunity];
    component.candidate = { candidateOpportunities } as Candidate;
    component.candidateSource = { sfJobOpp: jobOpp } as CandidateSource;
    expect(component.getCandidateOppForJobSource()).toEqual(candidateOpportunities[0]);
  });

  it('should determine if user can view private info', () => {
    component.candidate = {} as Candidate;
    authService.canViewPrivateCandidateInfo.and.returnValue(true);
    expect(component.canViewPrivateInfo()).toBeTrue();
    expect(authService.canViewPrivateCandidateInfo).toHaveBeenCalledWith(component.candidate);
  });

  describe('match explanation integration', () => {

    function findMatchExplanationEl() {
      return fixture.debugElement.query(By.css('app-candidate-match-explanation'));
    }

    const matching = {id: 5, matchingDescription: '<p>ICU nurse</p>', updatedDate: '2026-10-01T10:15:30Z'};

    it('should pass the candidate id and the Matching\'s id and updatedDate', () => {
      component.candidate = {id: 42, candidateOpportunities: []} as Candidate;
      component.candidateSource = {} as CandidateSource;
      component.matching = matching;
      fixture.detectChanges();

      const el = findMatchExplanationEl();
      expect(el.properties['candidateId']).toBe(42);
      expect(el.properties['matchingId']).toBe(5);
      expect(el.properties['matchingUpdatedDate']).toBe('2026-10-01T10:15:30Z');
    });

    it('should show the match explanation for a source with no job, as long as its Matching has a description', () => {
      component.candidate = {id: 42, candidateOpportunities: []} as Candidate;
      component.candidateSource = {} as CandidateSource;
      component.matching = matching;
      fixture.detectChanges();

      expect(findMatchExplanationEl()).toBeTruthy();
    });

    it('should NOT instantiate the match explanation component for a job source with no matching context', () => {
      component.candidate = {id: 42, candidateOpportunities: []} as Candidate;
      component.candidateSource = {sfJobOpp: {id: 7}} as CandidateSource;
      component.matching = null;
      fixture.detectChanges();

      expect(findMatchExplanationEl()).toBeFalsy();
    });

    it('should NOT instantiate the match explanation component when the Matching has no description', () => {
      component.candidate = {id: 42, candidateOpportunities: []} as Candidate;
      component.candidateSource = {} as CandidateSource;
      component.matching = {...matching, matchingDescription: '<p></p>'};
      fixture.detectChanges();

      expect(findMatchExplanationEl()).toBeFalsy();
    });

    it('should show the match explanation, passing the message, when the requirements have unsaved changes', () => {
      component.candidate = {id: 42, candidateOpportunities: []} as Candidate;
      component.candidateSource = {} as CandidateSource;
      component.matching = null;
      component.requirementsPendingMessage = 'Save the search';
      fixture.detectChanges();

      expect(findMatchExplanationEl().properties['requirementsPendingMessage'])
        .toBe('Save the search');
    });
  });

});
