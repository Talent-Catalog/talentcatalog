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

import {
  EditCandidateReviewStatusItemComponent
} from "./edit-candidate-review-status-item.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CandidateReviewStatusService} from "../../../../services/candidate-review-status.service";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SavedSearch} from "../../../../model/saved-search";
import {CandidateReviewStatusItem} from "../../../../model/candidate-review-status-item";
import {of, throwError} from "rxjs";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {UpdatedByComponent} from "../../user/updated-by/updated-by.component";

describe('EditCandidateReviewStatusItemComponent', () => {
  let component: EditCandidateReviewStatusItemComponent;
  let fixture: ComponentFixture<EditCandidateReviewStatusItemComponent>;
  let candidateReviewStatusService: jasmine.SpyObj<CandidateReviewStatusService>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;
  const mockCandidateReviewStatusItem = {
    id: 1,
    reviewStatus: 'verified',
    comment: 'Test comment',
    savedSearch: { id: 1 }
  } as CandidateReviewStatusItem;

  beforeEach(async () => {
    const candidateReviewStatusServiceSpy = jasmine.createSpyObj('CandidateReviewStatusService', ['get', 'create', 'update']);
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateReviewStatusItemComponent,UpdatedByComponent],
      imports: [ReactiveFormsModule,FormsModule,NgbModule,NgSelectModule,NgxWigModule],
      providers: [
        { provide: CandidateReviewStatusService, useValue: candidateReviewStatusServiceSpy },
        { provide: NgbActiveModal, useValue: activeModalSpy },
        UntypedFormBuilder
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditCandidateReviewStatusItemComponent);
    component = fixture.componentInstance;
    candidateReviewStatusService = TestBed.inject(CandidateReviewStatusService) as jasmine.SpyObj<CandidateReviewStatusService>;
    activeModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    component.candidateId = 123;
    component.savedSearch = { id: 1, name: 'Test Search' } as SavedSearch;
    component.candidateReviewStatusItemId = 1;
    candidateReviewStatusService.get.and.returnValue(of(mockCandidateReviewStatusItem));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and load candidate review status item if id is provided', () => {
    component.ngOnInit();
    expect(component.loading).toBeFalse();
    expect(component.form.controls['reviewStatus'].value).toEqual('verified');
    expect(component.form.controls['comment'].value).toEqual('Test comment');
  });

  it('should handle form validation', () => {
    component.ngOnInit();
    const reviewStatusControl = component.form.controls['reviewStatus'];
    reviewStatusControl.setValue('verified');
    expect(reviewStatusControl.valid).toBeTrue();
  });

  it('should create candidate review status item on save if no id is provided', () => {
    component.candidateReviewStatusItemId = null;
    const mockNewCandidateReviewStatusItem = {
      id: 2,
      reviewStatus: 'verified',
      comment: 'New comment',
      savedSearch: { id: 1 }
    } as CandidateReviewStatusItem;

    candidateReviewStatusService.create.and.returnValue(of(mockNewCandidateReviewStatusItem));

    component.ngOnInit();
    component.form.controls['reviewStatus'].setValue('verified');
    component.form.controls['comment'].setValue('New comment');

    component.onSave();
    expect(component.saving).toBeFalse();
    expect(candidateReviewStatusService.create).toHaveBeenCalledWith(component.form.value);
  });

  it('should update candidate review status item on save if id is provided', () => {
    const mockUpdatedCandidateReviewStatusItem = {
      id: 1,
      reviewStatus: 'verified',
      comment: 'Updated comment',
      savedSearch: { id: 1 }
    } as CandidateReviewStatusItem;

    candidateReviewStatusService.update.and.returnValue(of(mockUpdatedCandidateReviewStatusItem));

    component.ngOnInit();
    component.form.controls['reviewStatus'].setValue('verified');
    component.form.controls['comment'].setValue('Updated comment');

    component.onSave();
    expect(component.saving).toBeFalse();
    expect(candidateReviewStatusService.update).toHaveBeenCalledWith(1, component.form.value);
  });

  it('should handle save error', () => {
    candidateReviewStatusService.update.and.returnValue(throwError('Error'));

    component.ngOnInit();
    component.form.controls['reviewStatus'].setValue('verified');
    component.form.controls['comment'].setValue('Updated comment');

    component.onSave();
    expect(component.saving).toBeFalse();
    expect(candidateReviewStatusService.update).toHaveBeenCalledWith(1, component.form.value);
  });

  it('should close the modal on successful save', () => {
    const mockUpdatedCandidateReviewStatusItem = {
      id: 1,
      reviewStatus: 'verified',
      comment: 'Updated comment',
      savedSearch: { id: 1 }
    } as CandidateReviewStatusItem;

    candidateReviewStatusService.update.and.returnValue(of(mockUpdatedCandidateReviewStatusItem));

    component.ngOnInit();
    component.form.controls['reviewStatus'].setValue('verified');
    component.form.controls['comment'].setValue('Updated comment');

    component.onSave();
    expect(activeModal.close).toHaveBeenCalledWith(mockUpdatedCandidateReviewStatusItem);
  });

  it('should dismiss the modal on dismiss call', () => {
    component.dismiss();
    expect(activeModal.dismiss).toHaveBeenCalledWith(false);
  });
});
