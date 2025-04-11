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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { JobsComponent } from './jobs.component';
import { UntypedFormBuilder, ReactiveFormsModule } from "@angular/forms";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { SortedByComponent } from "../../util/sort/sorted-by.component";
import { NgbPaginationModule } from "@ng-bootstrap/ng-bootstrap";
import { NgSelectModule } from "@ng-select/ng-select";
import { SearchOppsBy } from "../../../model/base";
import { SearchJobRequest} from "../../../model/job";

class TestJobsComponent extends JobsComponent {
  // Expose createSearchRequest method
  public exposeCreateSearchRequest(): SearchJobRequest {
    return this.createSearchRequest();
  }
}
describe('JobsComponent', () => {
  let jobsComponent: TestJobsComponent;
  let fixture: ComponentFixture<TestJobsComponent>;
  let formBuilder: UntypedFormBuilder;

  // Setup for the test suite
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [TestJobsComponent, SortedByComponent],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        NgbPaginationModule,
        NgSelectModule
      ],
      providers: [
          { provide: UntypedFormBuilder, useClass: UntypedFormBuilder },
      ]
    }).compileComponents();
    formBuilder = TestBed.inject(UntypedFormBuilder);
   }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestJobsComponent);
    jobsComponent = fixture.componentInstance;
    jobsComponent.searchForm = formBuilder.group({
      keyword: [''],
      selectedStages: [[]],
      destinationIds: [[]]
    });
    // Set searchBy to live
    jobsComponent.searchBy = SearchOppsBy.live;
    // Detect changes
    fixture.detectChanges();
  });

  // Test cases
  it('should create', () => {
    expect(jobsComponent).toBeTruthy();
  });

  it('should call search function when the search form is submitted', () => {
    spyOn(jobsComponent, 'search');
    const form = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
    expect(jobsComponent.search).toHaveBeenCalled();
  });

  it('should generate correct search request for live search', () => {
    const searchRequest = jobsComponent.exposeCreateSearchRequest();
    expect(searchRequest.sfOppClosed).toBe(false);
    expect(searchRequest.activeStages).toBe(true);
  });

  it('should generate correct search request for starredByMe search', () => {
    jobsComponent.searchBy = SearchOppsBy.starredByMe;
    const searchRequest = jobsComponent.exposeCreateSearchRequest();
    expect(searchRequest.starred).toBe(true);
  });

});
