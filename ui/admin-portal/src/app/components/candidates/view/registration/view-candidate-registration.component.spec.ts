/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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
import {ViewCandidateRegistrationComponent} from "./view-candidate-registration.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {of} from "rxjs";

fdescribe('ViewCandidateRegistrationComponent', () => {
  let component: ViewCandidateRegistrationComponent;
  let fixture: ComponentFixture<ViewCandidateRegistrationComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateRegistrationComponent],
      providers: [
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy }
      ]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateRegistrationComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component correctly and load data', () => {

    // Simulate candidateService.get() returning mock data
    candidateService.get.and.returnValue(of(mockCandidate));

    // Set input properties
    component.candidate = mockCandidate;
    component.editable = true;

    // Trigger ngOnChanges manually
    component.ngOnChanges({  candidate: {
        currentValue: component.candidate,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }} );
    
    // Expect candidateService.get() to have been called with the correct candidate ID
    expect(candidateService.get).toHaveBeenCalledWith(mockCandidate.id);

    // Simulate candidateService.get() completing and emitting mock response data
    fixture.detectChanges();

    // Expect component's candidate property to be set with the response data
    expect(component.candidate).toEqual(mockCandidate);
  });
});
