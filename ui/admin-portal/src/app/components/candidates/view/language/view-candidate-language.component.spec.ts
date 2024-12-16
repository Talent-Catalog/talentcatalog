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
import {ViewCandidateLanguageComponent} from "./view-candidate-language.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateLanguageService} from "../../../../services/candidate-language.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateLanguage} from "../../../../model/candidate-language";
import {CandidateService} from "../../../../services/candidate.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ViewCandidateLanguageComponent', () => {
  let component: ViewCandidateLanguageComponent;
  let fixture: ComponentFixture<ViewCandidateLanguageComponent>;
  let mockCandidateLanguageService: jasmine.SpyObj<CandidateLanguageService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;

  const mockCandidate = new MockCandidate();
  const mockCandidateLanguages: CandidateLanguage[] = mockCandidate.candidateLanguages;

  beforeEach(async () => {
    const candidateLanguageServiceSpy = jasmine.createSpyObj('CandidateLanguageService', ['list', 'delete']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidate']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateLanguageComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: CandidateLanguageService, useValue: candidateLanguageServiceSpy },
        { provide: CandidateService, useValue: candidateServiceSpy },
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
});
