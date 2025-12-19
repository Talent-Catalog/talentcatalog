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
import {ViewCandidateRegistrationComponent} from "./view-candidate-registration.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('ViewCandidateRegistrationComponent', () => {
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
});
