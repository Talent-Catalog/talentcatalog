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
import {EditCandidateMediaWillingnessComponent} from "./edit-candidate-media-willingness.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {By} from "@angular/platform-browser";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";

describe('EditCandidateMediaWillingnessComponent', () => {
  let component: EditCandidateMediaWillingnessComponent;
  let fixture: ComponentFixture<EditCandidateMediaWillingnessComponent>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let fb : UntypedFormBuilder;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get', 'updateMedia']);
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateMediaWillingnessComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: activeModalSpy },
        { provide: CandidateService, useValue: candidateServiceSpy }
      ]
    })
    .compileComponents();

    fb = TestBed.inject(UntypedFormBuilder) as jasmine.SpyObj<UntypedFormBuilder>;
    mockActiveModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    mockCandidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateMediaWillingnessComponent);
    component = fixture.componentInstance;

    mockCandidateService.get.and.returnValue(of(mockCandidate));
    component.candidateId = 1;
    fixture.detectChanges();
  });

  it('should create', () => {
    mockCandidateService.get.and.returnValue(of(mockCandidate));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate media willingness', () => {
    mockCandidateService.get.and.returnValue(of(mockCandidate));

    fixture.detectChanges();

    expect(component.candidateForm).toBeDefined();
    expect(component.candidateForm.get('mediaWillingness').value).toEqual('Yes, open to media');
  });

  it('should call updateMedia on save and close the modal with updated candidate', () => {
    mockCandidateService.get.and.returnValue(of(mockCandidate));
    mockCandidateService.updateMedia.and.returnValue(of(mockCandidate));

    fixture.detectChanges();

    component.candidateForm.get('mediaWillingness').setValue('No');
    component.onSave();

    expect(mockCandidateService.updateMedia).toHaveBeenCalledWith(mockCandidate.id, { mediaWillingness: 'No' });
    expect(mockActiveModal.close).toHaveBeenCalledWith(mockCandidate);
  });

  it('should handle errors on save', () => {
    const errorResponse = { error: 'error message' };
    mockCandidateService.get.and.returnValue(of(mockCandidate));
    mockCandidateService.updateMedia.and.returnValue(throwError(errorResponse));

    fixture.detectChanges();

    component.candidateForm.get('mediaWillingness').setValue('No');
    component.onSave();

    expect(mockCandidateService.updateMedia).toHaveBeenCalledWith(1, { mediaWillingness: 'No' });
    expect(component.error).toEqual(errorResponse);
    expect(mockActiveModal.close).not.toHaveBeenCalled();
  });

  it('should dismiss the modal', () => {
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
  });

  it('should show loading spinner while loading', () => {
    component.loading = true;
    fixture.detectChanges();
    const spinner = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(spinner).toBeTruthy();
  });

  it('should show saving spinner while saving', () => {
    component.saving = true;
    fixture.detectChanges();
    const buttonSpinner = fixture.debugElement.query(By.css('button .fa-spinner'));
    expect(buttonSpinner).toBeTruthy();
  });
});
