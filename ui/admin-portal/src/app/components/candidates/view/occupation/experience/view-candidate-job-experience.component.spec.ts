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
import {ViewCandidateJobExperienceComponent} from "./view-candidate-job-experience.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateJobExperienceService
} from "../../../../../services/candidate-job-experience.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {CandidateJobExperience} from "../../../../../model/candidate-job-experience";
import {CandidateService} from "../../../../../services/candidate.service";

describe('ViewCandidateJobExperienceComponent', () => {
  let component: ViewCandidateJobExperienceComponent;
  let fixture: ComponentFixture<ViewCandidateJobExperienceComponent>;
  let mockCandidateJobExperienceService: jasmine.SpyObj<CandidateJobExperienceService>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let mockNgbModal: jasmine.SpyObj<NgbModal>;
  let formBuilder: UntypedFormBuilder;

  const mockCandidate = new MockCandidate();

  const mockExperiences: CandidateJobExperience[] = mockCandidate.candidateJobExperiences;
  const candidateOccupation = mockCandidate.candidateOccupations;

  beforeEach(async () => {
    const candidateJobExperienceServiceSpy = jasmine.createSpyObj('CandidateJobExperienceService', ['delete']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidate']);
    const ngbModalSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateJobExperienceComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateJobExperienceService, useValue: candidateJobExperienceServiceSpy },
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: NgbModal, useValue: ngbModalSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    mockCandidateJobExperienceService = TestBed.inject(CandidateJobExperienceService) as jasmine.SpyObj<CandidateJobExperienceService>;
    mockCandidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    mockNgbModal = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);

    fixture = TestBed.createComponent(ViewCandidateJobExperienceComponent);
    component = fixture.componentInstance;
  });

  beforeEach(() => {


    component.candidate = mockCandidate;
    component.candidateOccupation = candidateOccupation[0];
    component.editable = true;
    component.adminUser = true;

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
});
