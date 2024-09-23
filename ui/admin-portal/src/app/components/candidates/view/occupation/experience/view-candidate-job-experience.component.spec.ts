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
import {ViewCandidateJobExperienceComponent} from "./view-candidate-job-experience.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateJobExperienceService
} from "../../../../../services/candidate-job-experience.service";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {CandidateJobExperience} from "../../../../../model/candidate-job-experience";
import {SearchResults} from "../../../../../model/search-results";

describe('ViewCandidateJobExperienceComponent', () => {
  let component: ViewCandidateJobExperienceComponent;
  let fixture: ComponentFixture<ViewCandidateJobExperienceComponent>;
  let mockCandidateJobExperienceService: jasmine.SpyObj<CandidateJobExperienceService>;
  let mockNgbModal: jasmine.SpyObj<NgbModal>;
  let formBuilder: FormBuilder;

  const mockCandidate = new MockCandidate();

  const mockExperiences: CandidateJobExperience[] = mockCandidate.candidateJobExperiences;
  const candidateOccupation = mockCandidate.candidateOccupations;

  beforeEach(async () => {
    const candidateJobExperienceServiceSpy = jasmine.createSpyObj('CandidateJobExperienceService', ['search']);
    const ngbModalSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateJobExperienceComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        FormBuilder,
        { provide: CandidateJobExperienceService, useValue: candidateJobExperienceServiceSpy },
        { provide: NgbModal, useValue: ngbModalSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    mockCandidateJobExperienceService = TestBed.inject(CandidateJobExperienceService) as jasmine.SpyObj<CandidateJobExperienceService>;
    mockNgbModal = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    formBuilder = TestBed.inject(FormBuilder);

    fixture = TestBed.createComponent(ViewCandidateJobExperienceComponent);
    component = fixture.componentInstance;
  });

  beforeEach(() => {


    component.candidate = mockCandidate;
    component.candidateOccupation = candidateOccupation[0];
    component.editable = true;
    component.adminUser = true;

    const mockResults = {
      content: mockExperiences,
      totalPages: 1,
      number: 0
    } as SearchResults<CandidateJobExperience>;

    mockCandidateJobExperienceService.search.and.returnValue(of(mockResults));

    component.ngOnChanges({
      candidate: {
        previousValue: null,
        currentValue: mockCandidate,
        firstChange: true,
        isFirstChange: () => true
      }
    });
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.candidateJobExperienceForm.value).toEqual({
      candidateOccupationId: component.candidateOccupation.id,
      pageSize: 10,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: ['endDate']
    });
  });

  it('should retrieve and display candidate job experiences', () => {

    const mockResults = {
      content: mockExperiences,
      totalPages: 1,
      number: 0
    } as SearchResults<CandidateJobExperience>;

    mockCandidateJobExperienceService.search.and.returnValue(of(mockResults));

    component.doSearch();

    fixture.detectChanges();

    expect(component.experiences).toEqual(mockExperiences);
    expect(component.hasMore).toBe(false);
    expect(component.loading).toBe(false);

    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('div.col-sm-12').textContent).toContain('Software Developer');
  });

  it('should handle search error', () => {
    mockCandidateJobExperienceService.search.and.returnValue(throwError('Error'));

    component.doSearch();

    fixture.detectChanges();

    expect(component.error).toBe('Error');
    expect(component.loading).toBe(false);
  });
});
