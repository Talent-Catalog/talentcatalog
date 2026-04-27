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
import {ViewCandidateAttachmentComponent} from "./view-candidate-attachment.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateAttachmentService} from "../../../../services/candidate-attachment.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {UpdatedByComponent} from "../../../util/user/updated-by/updated-by.component";
import {ShareableDocsComponent} from "../shareable-docs/shareable-docs.component";
import {UserPipe} from "../../../util/user/user.pipe";
import {NgSelectModule} from "@ng-select/ng-select";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('ViewCandidateAttachmentComponent', () => {
  let component: ViewCandidateAttachmentComponent;
  let fixture: ComponentFixture<ViewCandidateAttachmentComponent>;
  let candidateAttachmentServiceSpy: jasmine.SpyObj<CandidateAttachmentService>;
  let fb: UntypedFormBuilder;

  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CandidateAttachmentService', ['searchPaged']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateAttachmentComponent, UpdatedByComponent, UserPipe, ShareableDocsComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateAttachmentService, useValue: spy },
        NgbModal
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fb = TestBed.inject(UntypedFormBuilder) as jasmine.SpyObj<UntypedFormBuilder>;
    candidateAttachmentServiceSpy = TestBed.inject(CandidateAttachmentService) as jasmine.SpyObj<CandidateAttachmentService>;

  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateAttachmentComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges(); // Run ngOnInit and ngOnChanges
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
