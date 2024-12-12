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
import {CandidateSearchCardComponent} from "./candidate-search-card.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AuthorizationService} from "../../../services/authorization.service";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {CandidateSource} from "../../../model/base";
import {Candidate} from "../../../model/candidate";
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {LocalStorageService} from "../../../services/local-storage.service";
import {CandidateService} from "../../../services/candidate.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('CandidateSearchCardComponent', () => {
  let component: CandidateSearchCardComponent;
  let fixture: ComponentFixture<CandidateSearchCardComponent>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const localStorageSpy = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['canViewPrivateCandidateInfo']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidate']);

    await TestBed.configureTestingModule({
      declarations: [CandidateSearchCardComponent],
      providers: [
        { provide: LocalStorageService, useValue: localStorageSpy },
        { provide: AuthorizationService, useValue: authSpy },
        { provide: CandidateService, userValue: candidateServiceSpy }
      ],
      imports: [NgbNavModule, HttpClientTestingModule],
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
    spyOn(component, 'isList' as never).and.returnValue(true as never);
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

});
