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

import {JoblinkComponent} from "./joblink.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {JobService} from "../../../services/job.service";
import {FormsModule} from "@angular/forms";
import {NgbTypeaheadModule, NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";
import {of} from "rxjs";
import {MockJob} from "../../../MockData/MockJob";
import {By} from "@angular/platform-browser";
import {MockSearchResults} from "../../../MockData/MockSearchResults";
import {SearchResults} from "../../../model/search-results";
import {EducationLevel} from "../../../model/education-level";
import {Job} from "../../../model/job";

describe('JoblinkComponent', () => {
  let component: JoblinkComponent;
  let fixture: ComponentFixture<JoblinkComponent>;
  let jobService: jasmine.SpyObj<JobService>;
  const searchResults: SearchResults<Job> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1, content: [MockJob] };
  beforeEach(async () => {
    const jobServiceSpy = jasmine.createSpyObj('JobService', ['searchPaged', 'get']);

    await TestBed.configureTestingModule({
      imports: [FormsModule, NgbTypeaheadModule],
      declarations: [ JoblinkComponent ],
      providers: [
        { provide: JobService, useValue: jobServiceSpy }
      ]
    })
    .compileComponents();

    jobService = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JoblinkComponent);
    component = fixture.componentInstance;
    jobService.get.and.returnValue(of(MockJob));
    jobService.searchPaged.and.returnValue(of(searchResults));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize correctly', () => {
    component.jobId = 1;
    component.currentJobName = 'XYZ';

    component.ngOnChanges({});

    expect(component.searchHeading).toBe('Change job association');
  });

  it('should emit job selection correctly', (done) => {
    const job = { name: 'Developer', id: 1 };
    component.jobSelection.subscribe(selectedJob => {
      expect(selectedJob).toEqual(job);
      done();
    });

    component.selectSearchResult({ item: job } as NgbTypeaheadSelectItemEvent);
  });
});
