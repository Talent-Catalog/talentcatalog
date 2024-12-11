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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {InputTextComponent} from '../../../../util/input/input-text/input-text.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ViewJobSuggestedSearchesComponent} from './view-job-suggested-searches.component';
import {JobService} from '../../../../../services/job.service';
import {SavedSearch} from "../../../../../model/saved-search";
import {Job} from '../../../../../model/job';
import {MockJob} from "../../../../../MockData/MockJob";
import {ActivatedRoute, Router} from "@angular/router";
import {of} from "rxjs";
import {MockSavedSearch} from "../../../../../MockData/MockSavedSearch";
import {JobPrepSuggestedSearches} from "../../../../../model/job-prep-item";
import {RouterLinkStubDirective} from "../../../../login/login.component.spec";

describe('ViewJobSuggestedSearchesComponent', () => {
  let component: ViewJobSuggestedSearchesComponent;
  let fixture: ComponentFixture<ViewJobSuggestedSearchesComponent>;
  let jobService: jasmine.SpyObj<JobService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  beforeEach(waitForAsync(() => {
    const jobServiceSpy = jasmine.createSpyObj('JobService', ['createSuggestedSearch', 'removeSuggestedSearch']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);
     TestBed.configureTestingModule({
      declarations: [ViewJobSuggestedSearchesComponent,RouterLinkStubDirective ,InputTextComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: JobService, useValue: jobServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy },
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
        { provide: Router, useValue: { navigateByUrl: jasmine.createSpy('navigateByUrl') } },
      ]
    }).compileComponents();

    jobService = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSuggestedSearchesComponent);
    component = fixture.componentInstance;

    // Mock job object
    const job: Job = MockJob;
    component.job = job;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add a suggested search to the job', () => {
    const mockSuffix = 'search 1';
    const mockSearch: SavedSearch = new MockSavedSearch();

      jobService.createSuggestedSearch.and.returnValue(of({ ...component.job, suggestedSearches: [mockSearch] }));

    // Create a mock modal instance
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: {
        title: '',
        message: ''
      },
      result: Promise.resolve(mockSuffix)
    };

    // Stub NgbModal open method to return the mock modal instance
    modalService.open.and.returnValue(mockModalRef as NgbModalRef);
      component.addSearch();
    // Expectations
    expect(modalService.open).toHaveBeenCalledWith(InputTextComponent, jasmine.any(Object));
    expect(mockModalRef.componentInstance.title).toEqual('Enter search name suffix');
    expect(mockModalRef.componentInstance.message).toEqual('(The search name will start with the job name. You just need to add a short suffix - eg "search 1" or "elastic search")');

    // Use fakeAsync to wait for the promise to resolve
    fixture.whenStable().then(() => {
      // Expectations after promise is resolved
      expect(jobService.createSuggestedSearch).toHaveBeenCalledWith(component.job.id, mockSuffix);
      expect(component.saving).toBeFalse();
      expect(component.error).toBeNull();
      expect(component.job.suggestedSearches).toEqual([mockSearch]);
    });
  });

  it('should remove a suggested search from the job', (() => {
    const mockSearch: SavedSearch = new MockSavedSearch();
    component.job.suggestedSearches = [mockSearch];

    // Spy on the jobUpdated event emitter
    spyOn(component.jobUpdated, 'emit').and.callThrough();

    // Set up the jobService spy to return an updated job after removal
    const updatedJob: Job = { ...component.job, suggestedSearches: [] };
    jobService.removeSuggestedSearch.and.returnValue(of(updatedJob));

    // Call the removeSearch method with the mock search
    component.removeSearch(mockSearch);

    // Expectations
    expect(jobService.removeSuggestedSearch).toHaveBeenCalledWith(component.job.id, mockSearch.id);

    component.job = updatedJob;
    // // Expectations after tick
    expect(component.saving).toBeFalse(); // Assuming saving is set to false after completion
    expect(component.error).toBeNull();
    expect(component.job.suggestedSearches).toEqual([]);
    expect(component.jobUpdated.emit).toHaveBeenCalledWith(updatedJob);
  }));


  it('should highlight searches correctly', () => {
    // Create a mock job object with suggested searches
    const mockJob = MockJob;
    mockJob.suggestedSearches = [new MockSavedSearch()]; // Mock data for suggested searches

    // Set the highlightItem to be a JobPrepSuggestedSearches instance
    component.highlightItem = new JobPrepSuggestedSearches();

    // Set the job input to the mock job object
    component.job = mockJob;

    // Expect the highlightSearches method to return true
    expect(component.highlightSearches()).toBeTrue();
  });

});
