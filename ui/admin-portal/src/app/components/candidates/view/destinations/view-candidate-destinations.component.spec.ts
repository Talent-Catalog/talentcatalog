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

import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateDestinationsComponent} from './view-candidate-destinations.component';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateDestinationService} from "../../../../services/candidate-destination.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateDestination} from "../../../../model/candidate";
import {of} from "rxjs";

describe('ViewCandidateDestinationsComponent', () => {
  let component: ViewCandidateDestinationsComponent;
  let fixture: ComponentFixture<ViewCandidateDestinationsComponent>;
  let candidateDestinationServiceSpy: jasmine.SpyObj<CandidateDestinationService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  const mockCandidate = new MockCandidate();
  const mockCandidateDestinations: CandidateDestination[] = mockCandidate.candidateDestinations;

  beforeEach(async () => {
    const candidateDestinationServiceSpyObj = jasmine.createSpyObj('CandidateDestinationService', ['list']);
    const modalServiceSpyObj = jasmine.createSpyObj('NgbModal', ['open']);
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [ ViewCandidateDestinationsComponent ],
      providers: [
        { provide: CandidateDestinationService, useValue: candidateDestinationServiceSpyObj },
        { provide: NgbModal, useValue: modalServiceSpyObj }
      ]
    })
    .compileComponents();

    candidateDestinationServiceSpy = TestBed.inject(CandidateDestinationService) as jasmine.SpyObj<CandidateDestinationService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateDestinationsComponent);
    component = fixture.componentInstance;
    component.editable = true; // Mocking editable input
    component.candidate = mockCandidate;
    component.candidateDestinations = mockCandidateDestinations;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should load candidate destinations on ngOnChanges', () => {
    candidateDestinationServiceSpy.list.and.returnValue(of(mockCandidateDestinations));

    component.ngOnChanges({});
    expect(component.loading).toBeFalsy();
    expect(component.error).toBeUndefined();
    expect(component.candidateDestinations).toEqual(mockCandidateDestinations);
  });
});
