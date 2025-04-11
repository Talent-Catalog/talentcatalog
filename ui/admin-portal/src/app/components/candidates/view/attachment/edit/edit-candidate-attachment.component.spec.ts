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
import {EditCandidateAttachmentComponent} from "./edit-candidate-attachment.component";
import {
  CandidateAttachmentService,
  UpdateCandidateAttachmentRequest
} from "../../../../../services/candidate-attachment.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {
  FormsModule,
  ReactiveFormsModule,
  UntypedFormBuilder,
  UntypedFormControl,
  Validators
} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {of, throwError} from "rxjs";
import {AttachmentType} from "../../../../../model/candidate-attachment";

describe('EditCandidateAttachmentComponent', () => {
  let component: EditCandidateAttachmentComponent;
  let fixture: ComponentFixture<EditCandidateAttachmentComponent>;
  let candidateAttachmentServiceSpy: jasmine.SpyObj<CandidateAttachmentService>;
  let fb: UntypedFormBuilder;
  const mockAttachment = new MockCandidate().candidateAttachments[0];
  beforeEach(async () => {

    const candidateAttachmentServiceMock = jasmine.createSpyObj('CandidateAttachmentService', ['updateAttachment']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateAttachmentComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        NgbActiveModal,
        { provide: CandidateAttachmentService, useValue: candidateAttachmentServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditCandidateAttachmentComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder) as jasmine.SpyObj<UntypedFormBuilder>;
    candidateAttachmentServiceSpy = TestBed.inject(CandidateAttachmentService) as jasmine.SpyObj<CandidateAttachmentService>;
    // Initialize the form with mock data
    mockAttachment.type = AttachmentType.link;
    component.attachment = mockAttachment;
    component.form = fb.group({
      id: [mockAttachment.id],
      name: [mockAttachment.name, Validators.required],
      location: [mockAttachment.location]
    });

    if (mockAttachment.type === 'link') {
      component.form.addControl('location', new UntypedFormControl(mockAttachment.location, Validators.required));
    }
    fixture.detectChanges();
  });

  it('should ', () => {
    expect(component).toBeTruthy();
  });

  it('should create and initialize form with attachment data', () => {
    expect(component).toBeTruthy();
    expect(component.form).toBeDefined();
    expect(component.form.controls['name'].value).toBe(mockAttachment.name);
    expect(component.form.controls['location'].value).toBe(mockAttachment.location);
  });

  it('should save the attachment', () => {
    const mockResponse = { ...mockAttachment, name: 'Updated Attachment' };
    candidateAttachmentServiceSpy.updateAttachment.and.returnValue(of(mockResponse));

    component.form.controls['name'].setValue('Updated Attachment');
    component.form.controls['location'].setValue('https://updated.com');

    component.save();

    expect(candidateAttachmentServiceSpy.updateAttachment).toHaveBeenCalledWith(
      mockAttachment.id,
      jasmine.objectContaining<UpdateCandidateAttachmentRequest>({
        name: 'Updated Attachment',
        location: 'https://updated.com'
      })
    );
    expect(component.loading).toBe(true);
  });

  it('should handle error during save', () => {
    const mockError = 'Update failed';
    candidateAttachmentServiceSpy.updateAttachment.and.returnValue(throwError(mockError));

    component.form.controls['name'].setValue('Updated Attachment');
    component.form.controls['location'].setValue('https://updated.com');

    component.save();

    expect(candidateAttachmentServiceSpy.updateAttachment).toHaveBeenCalledWith(
      mockAttachment.id,
      jasmine.objectContaining<UpdateCandidateAttachmentRequest>({
        name: 'Updated Attachment',
        location: 'https://updated.com'
      })
    );
    expect(component.loading).toBe(true);
    expect(component.error).toBe(mockError);
  });
});
