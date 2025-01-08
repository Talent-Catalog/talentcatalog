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
import {ViewCandidateOccupationComponent} from "./view-candidate-occupation.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateOccupationService} from "../../../../services/candidate-occupation.service";
import {CandidateJobExperienceService} from "../../../../services/candidate-job-experience.service";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {
  ViewCandidateJobExperienceComponent
} from "./experience/view-candidate-job-experience.component";

describe('ViewCandidateOccupationComponent', () => {
  let component: ViewCandidateOccupationComponent;
  let fixture: ComponentFixture<ViewCandidateOccupationComponent>;
  let mockModalService: any;
  let mockCandidateService: any;
  let mockCandidateOccupationService: any;
  let mockCandidateJobExperienceService: any;
  const mockCandidate = new MockCandidate();
  beforeEach(waitForAsync(() => {
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['get']);
    mockCandidateOccupationService = jasmine.createSpyObj('CandidateOccupationService', ['get']);
    mockCandidateJobExperienceService = jasmine.createSpyObj('CandidateJobExperienceService', ['search']);

    TestBed.configureTestingModule({
      declarations: [ ViewCandidateOccupationComponent, ViewCandidateJobExperienceComponent ],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      // schemas: [NO_ERRORS_SCHEMA],
      providers: [
        UntypedFormBuilder,
        { provide: NgbModal, useValue: mockModalService },
        { provide: CandidateService, useValue: mockCandidateService },
        { provide: CandidateOccupationService, useValue: mockCandidateOccupationService },
        { provide: CandidateJobExperienceService, useValue: mockCandidateJobExperienceService }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateOccupationComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize correctly and set up experiences', () => {
    // Trigger ngOnChanges manually
    component.ngOnChanges({  candidate: {
        currentValue: component.candidate,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }} );

    expect(component.orderOccupation).toBe(true);
    expect(component.experiences).toEqual(component.candidate.candidateJobExperiences);
  });
});
