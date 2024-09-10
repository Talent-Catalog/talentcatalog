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
import {ViewCandidateAttachmentComponent} from "./view-candidate-attachment.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateAttachmentService} from "../../../../services/candidate-attachment.service";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {of} from "rxjs";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {MockUser} from "../../../../MockData/MockUser";
import {UpdatedByComponent} from "../../../util/user/updated-by/updated-by.component";
import {ShareableDocsComponent} from "../shareable-docs/shareable-docs.component";
import {UserPipe} from "../../../util/user/user.pipe";
import {NgSelectModule} from "@ng-select/ng-select";

describe('ViewCandidateAttachmentComponent', () => {
  let component: ViewCandidateAttachmentComponent;
  let fixture: ComponentFixture<ViewCandidateAttachmentComponent>;
  let candidateAttachmentServiceSpy: jasmine.SpyObj<CandidateAttachmentService>;
  let fb: FormBuilder;

  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CandidateAttachmentService', ['searchPaged']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateAttachmentComponent, UpdatedByComponent, UserPipe, ShareableDocsComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        FormBuilder,
        { provide: CandidateAttachmentService, useValue: spy },
        NgbModal
      ]
    })
    .compileComponents();

    fb = TestBed.inject(FormBuilder) as jasmine.SpyObj<FormBuilder>;
    candidateAttachmentServiceSpy = TestBed.inject(CandidateAttachmentService) as jasmine.SpyObj<CandidateAttachmentService>;
    candidateAttachmentServiceSpy.searchPaged.and.returnValue(of());

  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateAttachmentComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.attachments = mockCandidate.candidateAttachments;
    // Initialize the form
    component.attachmentForm = fb.group({
      pageNumber: 0
    });
    fixture.detectChanges(); // Run ngOnInit and ngOnChanges
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load more attachments when "Load More" button is clicked', () => {
    // Initial page number should be 0
    expect(component.attachmentForm.value.pageNumber).toBe(0);

    // Click the "Load More" button
    component.loadMore();

    // Expect the page number to increment
    expect(component.attachmentForm.value.pageNumber).toBe(1);

    // Verify that the searchPaged method is called again with the updated form value
    expect(candidateAttachmentServiceSpy.searchPaged).toHaveBeenCalledTimes(1);

    // Check that more attachments are added to the list
    expect(component.attachments.length).toBe(2);
  });
});
