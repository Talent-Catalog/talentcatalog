import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {of, throwError} from 'rxjs';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

import {EditCandidateAspirationsComponent} from './edit-candidate-aspirations.component';
import {CandidateService} from '../../../../../services/candidate.service';

describe('EditCandidateAspirationsComponent', () => {
  let component: EditCandidateAspirationsComponent;
  let fixture: ComponentFixture<EditCandidateAspirationsComponent>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  const candidateMock: any = {
    id: 123,
    aspirations: 'I want to become a software engineer.'
  };

  beforeEach(async () => {
    activeModal = jasmine.createSpyObj<NgbActiveModal>('NgbActiveModal', [
      'close',
      'dismiss'
    ]);

    candidateService = jasmine.createSpyObj<CandidateService>('CandidateService', [
      'get',
      'updateAspirations'
    ]);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateAspirationsComponent],
      imports: [ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        {provide: NgbActiveModal, useValue: activeModal},
        {provide: CandidateService, useValue: candidateService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(EditCandidateAspirationsComponent);
    component = fixture.componentInstance;

    component.candidateId = candidateMock.id;
    candidateService.get.and.returnValue(of(candidateMock));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load candidate aspirations on init', () => {
    expect(candidateService.get).toHaveBeenCalledWith(candidateMock.id);
    expect(component.candidateForm).toBeTruthy();
    expect(component.candidateForm.value).toEqual({
      aspirations: candidateMock.aspirations
    });
    expect(component.loading).toBeFalse();
  });

  it('should render the aspirations form control after loading', () => {
    fixture.detectChanges();

    const textarea = fixture.debugElement.query(By.css('#aspirations'));

    expect(textarea).toBeTruthy();
  });

  it('should call onSave when modal action is emitted', () => {
    spyOn(component, 'onSave');

    const modal = fixture.debugElement.query(By.css('tc-modal'));
    modal.triggerEventHandler('onAction', null);

    expect(component.onSave).toHaveBeenCalled();
  });

  it('should update aspirations and close modal on save success', () => {
    const updatedCandidate: any = {
      ...candidateMock,
      aspirations: 'Updated aspirations'
    };

    component.candidateForm.setValue({
      aspirations: 'Updated aspirations'
    });

    candidateService.updateAspirations.and.returnValue(of(updatedCandidate));

    component.onSave();

    expect(component.saving).toBeFalse();
    expect(candidateService.updateAspirations).toHaveBeenCalledWith(
      candidateMock.id,
      {
        aspirations: 'Updated aspirations'
      }
    );
    expect(activeModal.close).toHaveBeenCalledWith(updatedCandidate);
  });

  it('should set error and stop saving when save fails', () => {
    const errorMessage = 'Unable to update aspirations';

    component.candidateForm.setValue({
      aspirations: 'Updated aspirations'
    });

    candidateService.updateAspirations.and.returnValue(
      throwError(errorMessage)
    );

    component.onSave();

    expect(component.saving).toBeFalse();
    expect(component.error).toBe(errorMessage);
    expect(activeModal.close).not.toHaveBeenCalled();
  });

  it('should close modal with candidate', () => {
    component.closeModal(candidateMock);

    expect(activeModal.close).toHaveBeenCalledWith(candidateMock);
  });

  it('should dismiss modal with false', () => {
    component.dismiss();

    expect(activeModal.dismiss).toHaveBeenCalledWith(false);
  });
});
