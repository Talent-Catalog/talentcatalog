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
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {ViewCandidateEducationComponent} from "./view-candidate-education.component";
import {CandidateEducationService} from "../../../../services/candidate-education.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateEducation} from "../../../../model/candidate-education";
import {of, throwError} from "rxjs";

describe('ViewCandidateEducationComponent', () => {
  let component: ViewCandidateEducationComponent;
  let fixture: ComponentFixture<ViewCandidateEducationComponent>;
  let mockCandidateEducationService: jasmine.SpyObj<CandidateEducationService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateEducationServiceSpy = jasmine.createSpyObj('CandidateEducationService', ['list']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateEducationComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: CandidateEducationService, useValue: candidateEducationServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy }
      ]
    })
    .compileComponents();

    mockCandidateEducationService = TestBed.inject(CandidateEducationService) as jasmine.SpyObj<CandidateEducationService>;
    mockModalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateEducationComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should load candidate education details', () => {
    const mockEducations: CandidateEducation[] = mockCandidate.candidateEducations;

    mockCandidateEducationService.list.and.returnValue(of(mockEducations));

    component.search();

    expect(component.loading).toBeFalse();
    expect(component.candidateEducations).toEqual(mockEducations);
  });

  it('should handle error when loading candidate education details', () => {
    const error = 'Failed to load education details';
    mockCandidateEducationService.list.and.returnValue(throwError(error));

    component.search();

    expect(component.loading).toBeFalse();
    expect(component.error).toBe(error);
    expect(component.candidateEducations).toBeUndefined();
  });
});
