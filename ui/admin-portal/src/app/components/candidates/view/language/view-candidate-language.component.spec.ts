/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {ViewCandidateLanguageComponent} from "./view-candidate-language.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {CandidateLanguageService} from "../../../../services/candidate-language.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateLanguage} from "../../../../model/candidate-language";
import {of, throwError} from "rxjs";

fdescribe('ViewCandidateLanguageComponent', () => {
  let component: ViewCandidateLanguageComponent;
  let fixture: ComponentFixture<ViewCandidateLanguageComponent>;
  let mockCandidateLanguageService: jasmine.SpyObj<CandidateLanguageService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;

  const mockCandidate = new MockCandidate();
  const mockCandidateLanguages: CandidateLanguage[] = mockCandidate.candidateLanguages;

  beforeEach(async () => {
    const candidateLanguageServiceSpy = jasmine.createSpyObj('CandidateLanguageService', ['list', 'delete']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateLanguageComponent],
      providers: [
        { provide: CandidateLanguageService, useValue: candidateLanguageServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy }
      ]
    }).compileComponents();

    mockCandidateLanguageService = TestBed.inject(CandidateLanguageService) as jasmine.SpyObj<CandidateLanguageService>;
    mockModalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateLanguageComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.candidateLanguages = mockCandidateLanguages;
    component.loading = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display loading spinner initially', () => {
    expect(fixture.nativeElement.querySelector('.fa-spinner')).toBeTruthy();
  });


  it('should fetch candidate languages on initialization', fakeAsync(() => {
    mockCandidateLanguageService.list.and.returnValue(of(mockCandidateLanguages));

    component.search();
    tick();

    expect(component.candidateLanguages).toEqual(mockCandidateLanguages);
    expect(mockCandidateLanguageService.list).toHaveBeenCalledWith(mockCandidate.id);
  }));

  it('should handle error when fetching candidate languages', fakeAsync(() => {
    const errorMessage = 'Error fetching candidate languages';
    mockCandidateLanguageService.list.and.returnValue(throwError(errorMessage));

    component.search();
    tick();

    expect(component.error).toEqual(errorMessage);
    expect(mockCandidateLanguageService.list).toHaveBeenCalledWith(mockCandidate.id);
  }));
});
