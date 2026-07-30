/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */


import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {ViewCandidateDestinationsComponent} from './view-candidate-destinations.component';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateDestinationService} from "../../../../services/candidate-destination.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateDestination} from "../../../../model/candidate";
import {of, throwError} from "rxjs";
import {CandidateService} from "../../../../services/candidate.service";
import {SimpleChange} from "@angular/core";
import {
  EditCandidateDestinationsComponent
} from "./edit/edit-candidate-destinations/edit-candidate-destinations.component";

describe('ViewCandidateDestinationsComponent', () => {
  let component: ViewCandidateDestinationsComponent;
  let fixture: ComponentFixture<ViewCandidateDestinationsComponent>;
  let candidateDestinationServiceSpy: jasmine.SpyObj<CandidateDestinationService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  const mockCandidate = new MockCandidate();
  const mockCandidateDestinations: CandidateDestination[] = mockCandidate.candidateDestinations;

  beforeEach(async () => {
    const candidateDestinationServiceSpyObj = jasmine.createSpyObj('CandidateDestinationService', ['list']);
    const modalServiceSpyObj = jasmine.createSpyObj('NgbModal', ['open']);
    const candidateServiceSpyObj = jasmine.createSpyObj('CandidateService', ['updateCandidate']);
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [ ViewCandidateDestinationsComponent ],
      providers: [
        { provide: CandidateDestinationService, useValue: candidateDestinationServiceSpyObj },
        { provide: NgbModal, useValue: modalServiceSpyObj },
        { provide: CandidateService, useValue: candidateServiceSpyObj }
      ]
    })
    .compileComponents();

    candidateDestinationServiceSpy = TestBed.inject(CandidateDestinationService) as jasmine.SpyObj<CandidateDestinationService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
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

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should check destinations when candidate changes', () => {
    const destinations = [
      {
        id: 1,
        interest: 'Yes',
        notes: 'Interested'
      }
    ] as any;

    const checkSpy = spyOn(component, 'checkForEmptyDestinations')
    .and.callThrough();

    component.ngOnChanges({
      candidate: new SimpleChange(
        {candidateDestinations: []},
        {candidateDestinations: destinations},
        false
      )
    });

    expect(checkSpy).toHaveBeenCalledWith(destinations);
    expect(component.emptyDestinations).toBeFalse();
  });

  it('should not check destinations when changes are empty', () => {
    const checkSpy = spyOn(component, 'checkForEmptyDestinations');

    component.ngOnChanges({});

    expect(checkSpy).not.toHaveBeenCalled();
  });

  it('should not check destinations when candidate value has not changed', () => {
    const sameCandidate = {
      candidateDestinations: []
    };

    const checkSpy = spyOn(component, 'checkForEmptyDestinations');

    component.ngOnChanges({
      candidate: new SimpleChange(
        sameCandidate,
        sameCandidate,
        false
      )
    });

    expect(checkSpy).not.toHaveBeenCalled();
  });

  it('should not check destinations when current destinations are null', () => {
    const checkSpy = spyOn(component, 'checkForEmptyDestinations');

    component.ngOnChanges({
      candidate: new SimpleChange(
        {candidateDestinations: []},
        {candidateDestinations: null},
        false
      )
    });

    expect(checkSpy).not.toHaveBeenCalled();
  });

  it('should search and populate destinations successfully', () => {
    const destinations = [
      {
        id: 1,
        interest: 'Yes',
        notes: 'Open to relocation'
      }
    ] as any;

    candidateDestinationServiceSpy.list.and.returnValue(
      of(destinations)
    );

    component.loading = false;

    component.doSearch();

    expect(candidateDestinationServiceSpy.list)
    .toHaveBeenCalledWith(component.candidate.id);
    expect(component.candidateDestinations)
    .toEqual(destinations);
    expect(component.loading).toBeFalse();
    expect(component.emptyDestinations).toBeFalse();
  });

  it('should handle destination search error', () => {
    const error = new Error('search failed');

    candidateDestinationServiceSpy.list.and.returnValue(
      throwError(error)
    );

    component.doSearch();

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();
  });

  it('should open edit modal and refresh candidate after success', fakeAsync(() => {
    const destination = {
      id: 5,
      interest: 'Unsure',
      notes: 'Needs more information'
    } as any;

    const modalRef = {
      componentInstance: {},
      result: Promise.resolve(destination)
    } as any;

    modalServiceSpy.open.and.returnValue(modalRef);

    component.editDestinationsDetails(destination);
    tick();

    expect(modalServiceSpy.open).toHaveBeenCalledWith(
      EditCandidateDestinationsComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );
    expect(modalRef.componentInstance.candidateDestination)
    .toBe(destination);
    expect(candidateServiceSpy.updateCandidate)
    .toHaveBeenCalledTimes(1);
  }));

  it('should ignore edit modal dismissal', fakeAsync(() => {
    const destination = {
      id: 5,
      interest: 'No',
      notes: null
    } as any;

    const modalRef = {
      componentInstance: {},
      result: Promise.reject('dismissed')
    } as any;

    modalServiceSpy.open.and.returnValue(modalRef);

    component.editDestinationsDetails(destination);
    tick();

    expect(modalRef.componentInstance.candidateDestination)
    .toBe(destination);
    expect(candidateServiceSpy.updateCandidate)
    .not.toHaveBeenCalled();
  }));

  it('should mark destinations empty when array is empty', () => {
    const result = component.checkForEmptyDestinations([]);

    expect(result).toBeTrue();
    expect(component.emptyDestinations).toBeTrue();
  });

  it('should mark destinations empty when all interest and notes values are null', () => {
    const destinations = [
      {
        interest: null,
        notes: null
      },
      {
        interest: null,
        notes: null
      }
    ] as any;

    const result = component.checkForEmptyDestinations(
      destinations
    );

    expect(result).toBeTrue();
    expect(component.emptyDestinations).toBeTrue();
  });

  it('should mark destinations non-empty when at least one interest is present', () => {
    const destinations = [
      {
        interest: null,
        notes: null
      },
      {
        interest: 'Yes',
        notes: null
      }
    ] as any;

    const result = component.checkForEmptyDestinations(
      destinations
    );

    expect(result).toBeFalse();
    expect(component.emptyDestinations).toBeFalse();
  });

  it('should mark destinations non-empty when at least one note is present', () => {
    const destinations = [
      {
        interest: null,
        notes: null
      },
      {
        interest: null,
        notes: 'Preferred destination'
      }
    ] as any;

    const result = component.checkForEmptyDestinations(
      destinations
    );

    expect(result).toBeFalse();
    expect(component.emptyDestinations).toBeFalse();
  });

});
