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
import {CreateCandidateAttachmentComponent} from "./create-candidate-attachment.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {
  AttachmentType,
  CandidateAttachmentRequest
} from "../../../../../model/candidate-attachment";
import {UploadType} from "../../../../../model/task";
import {of, throwError} from "rxjs";

describe('CreateCandidateAttachmentComponent', () => {
  let component: CreateCandidateAttachmentComponent;
  let fixture: ComponentFixture<CreateCandidateAttachmentComponent>;
  let candidateAttachmentServiceSpy: jasmine.SpyObj<CandidateAttachmentService>;
  let modalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const candidateAttachmentServiceMock = jasmine.createSpyObj('CandidateAttachmentService', ['createAttachment']);
    const modalMock = jasmine.createSpyObj('NgbActiveModal', ['close']);

    await TestBed.configureTestingModule({
      declarations: [ CreateCandidateAttachmentComponent ],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateAttachmentService, useValue: candidateAttachmentServiceMock },
        { provide: NgbActiveModal, useValue: modalMock }
      ]
    })
    .compileComponents();

    candidateAttachmentServiceSpy = TestBed.inject(CandidateAttachmentService) as jasmine.SpyObj<CandidateAttachmentService>;
    modalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateAttachmentComponent);
    component = fixture.componentInstance;
    component.candidateId = 1;
    component.type = 'link';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should save a link attachment', () => {
    // Arrange
    const mockAttachment: CandidateAttachmentRequest = {
      candidateId: 1,
      type: AttachmentType.link,
      name: 'Test Attachment',
      location: 'https://example.com',
      cv: false,
      uploadType: UploadType.other
    };

    // @ts-expect-error
    candidateAttachmentServiceSpy.createAttachment.and.returnValue(of(mockAttachment));
    // Fill the form
    component.form.controls['name'].setValue('Test Attachment');
    component.form.controls['location'].setValue('https://example.com');

    // Act
    component.save();

    expect(candidateAttachmentServiceSpy.createAttachment).toHaveBeenCalledWith(jasmine.objectContaining(mockAttachment));
    expect(modalSpy.close).toHaveBeenCalled();
    expect(component.error).toBeFalsy();
  });

  it('should handle save link attachment error', () => {
    // Arrange
    candidateAttachmentServiceSpy.createAttachment.and.returnValue(throwError('Error saving attachment'));

    // Fill the form
    component.form.controls['name'].setValue('Test Attachment');
    component.form.controls['location'].setValue('https://example.com');

    // Act
    component.save();

    // Assert
    expect(candidateAttachmentServiceSpy.createAttachment).toHaveBeenCalled();
    expect(component.error).toBe('Error saving attachment');
    expect(modalSpy.close).not.toHaveBeenCalled();
  });
});
