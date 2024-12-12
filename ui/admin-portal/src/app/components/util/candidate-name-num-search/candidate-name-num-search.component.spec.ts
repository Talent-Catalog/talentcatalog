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
import {CandidateNameNumSearchComponent} from "./candidate-name-num-search.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../services/candidate.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {Router} from "@angular/router";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {ReactiveFormsModule} from "@angular/forms";
import {By} from "@angular/platform-browser";
import {SearchResults} from "../../../model/search-results";
import {Candidate} from "../../../model/candidate";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

describe('CandidateNameNumSearchComponent', () => {
  let component: CandidateNameNumSearchComponent;
  let fixture: ComponentFixture<CandidateNameNumSearchComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let router: jasmine.SpyObj<Router>;

  const mockCandidate = new MockCandidate();
  const mockResults: SearchResults<Candidate> = {
    first: false, last: false, number: 0, size: 0,
    content: [mockCandidate],
    totalElements: 1,
    totalPages: 1
  };
  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['findByCandidateNumberOrName']);
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['canViewCandidateName']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [CandidateNameNumSearchComponent],
      imports: [ReactiveFormsModule,NgbModule],
      providers: [
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: AuthorizationService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateNameNumSearchComponent);
    component = fixture.componentInstance;
    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  //
  it('should display the correct placeholder text when displayValue is null and user is limited', () => {
    authService.canViewCandidateName.and.returnValue(false);
    component.displayValue = null;
    fixture.detectChanges();
    expect(component.placeholderText).toBe('Candidate number...');
  });
  //
  it('should display the correct placeholder text when displayValue is null and user is not limited', () => {
    authService.canViewCandidateName.and.returnValue(true);
    component.displayValue = null;
    fixture.detectChanges();
    expect(component.placeholderText).toBe('Name or number...');
  });

  it('should render candidate row correctly when user is limited', () => {
    authService.canViewCandidateName.and.returnValue(false);
    const row = component.renderCandidateRow(mockCandidate);
    expect(row).toBe(mockCandidate.candidateNumber);
  });

  it('should render candidate row correctly when user is not limited', () => {
    authService.canViewCandidateName.and.returnValue(true);
    const row = component.renderCandidateRow(mockCandidate);
    expect(row).toBe(`${mockCandidate.candidateNumber}: ${mockCandidate.user.firstName} ${mockCandidate.user.lastName}`);
  });

  it('should handle select search result correctly when handleSelect is displayOnly', () => {
    component.handleSelect = 'displayOnly';
    spyOn(component.candChange, 'emit');
    authService.canViewCandidateName.and.returnValue(true);

    const input = fixture.debugElement.query(By.css('#quickNumberOrNameSearch')).nativeElement;
    component.selectSearchResult({ preventDefault: () => {}, item: mockCandidate }, input);

    expect(input.value).toBe(`${mockCandidate.candidateNumber}: ${mockCandidate.user.firstName} ${mockCandidate.user.lastName}`);
    expect(component.candChange.emit).toHaveBeenCalledWith(mockCandidate as unknown as string);
  });

  it('should handle select search result correctly when handleSelect is not displayOnly', () => {
    component.handleSelect = 'navigate';
    const input = fixture.debugElement.query(By.css('#quickNumberOrNameSearch')).nativeElement;
    component.selectSearchResult({ preventDefault: () => {}, item: mockCandidate }, input);
    expect(input.value).toBe('');
    expect(router.navigate).toHaveBeenCalledWith(['candidate', mockCandidate.candidateNumber]);
  });
});
