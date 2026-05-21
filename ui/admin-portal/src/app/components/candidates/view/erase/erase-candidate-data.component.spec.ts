/*
 * Copyright (c) 2026 Talent Catalog.
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

import {of, throwError} from 'rxjs';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {
  Candidate,
  EraseCandidateRequest,
  EraseCandidateResponse
} from '../../../../model/candidate';
import {CandidateService} from '../../../../services/candidate.service';
import {EraseCandidateDataComponent} from './erase-candidate-data.component';

describe('EraseCandidateDataComponent', () => {
  let component: EraseCandidateDataComponent;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  const candidate = {
    id: 123,
    candidateNumber: '909',
    user: {
      firstName: 'Jane',
      lastName: 'Doe'
    }
  } as Candidate;

  beforeEach(() => {
    activeModal = jasmine.createSpyObj<NgbActiveModal>('NgbActiveModal', [
      'dismiss',
      'close'
    ]);

    candidateService = jasmine.createSpyObj<CandidateService>('CandidateService', [
      'eraseCandidate'
    ]);

    component = new EraseCandidateDataComponent(activeModal, candidateService);
    component.candidate = candidate;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('cancel', () => {
    it('should dismiss the modal when not erasing', () => {
      component.erasing = false;

      component.cancel();

      expect(activeModal.dismiss).toHaveBeenCalled();
    });

    it('should not dismiss the modal when erasing', () => {
      component.erasing = true;

      component.cancel();

      expect(activeModal.dismiss).not.toHaveBeenCalled();
    });
  });

  describe('next', () => {
    it('should move from step 1 to step 2 and clear error', () => {
      component.step = 1;
      component.error = 'Old error';

      component.next();

      expect(component.step).toBe(2);
      expect(component.error).toBeNull();
    });

    it('should not move past step 2', () => {
      component.step = 2;

      component.next();

      expect(component.step).toBe(2);
    });
  });

  describe('back', () => {
    it('should move from step 2 to step 1 and clear error', () => {
      component.step = 2;
      component.error = 'Old error';

      component.back();

      expect(component.step).toBe(1);
      expect(component.error).toBeNull();
    });

    it('should not move before step 1', () => {
      component.step = 1;

      component.back();

      expect(component.step).toBe(1);
    });
  });

  describe('canConfirm', () => {
    it('should return true when candidate number matches and irreversible confirmation is checked', () => {
      component.erasing = false;
      component.irreversibleConfirmed = true;
      component.confirmationCandidateNumber = '909';

      expect(component.canConfirm()).toBeTrue();
    });

    it('should trim candidate number before checking', () => {
      component.erasing = false;
      component.irreversibleConfirmed = true;
      component.confirmationCandidateNumber = ' 909 ';

      expect(component.canConfirm()).toBeTrue();
    });

    it('should return false when erasing', () => {
      component.erasing = true;
      component.irreversibleConfirmed = true;
      component.confirmationCandidateNumber = '909';

      expect(component.canConfirm()).toBeFalse();
    });

    it('should return false when irreversible confirmation is not checked', () => {
      component.erasing = false;
      component.irreversibleConfirmed = false;
      component.confirmationCandidateNumber = '909';

      expect(component.canConfirm()).toBeFalse();
    });

    it('should return false when candidate number does not match', () => {
      component.erasing = false;
      component.irreversibleConfirmed = true;
      component.confirmationCandidateNumber = '123';

      expect(component.canConfirm()).toBeFalse();
    });
  });

  describe('candidateName', () => {
    it('should return candidate full name', () => {
      expect(component.candidateName()).toBe('Jane Doe');
    });

    it('should return this candidate when user name is missing', () => {
      component.candidate = {
        id: 123,
        candidateNumber: '909',
        user: {}
      } as Candidate;

      expect(component.candidateName()).toBe('this candidate');
    });
  });

  describe('confirm', () => {
    it('should set error and not call service when confirmation is invalid', () => {
      component.confirmationCandidateNumber = 'wrong';
      component.irreversibleConfirmed = true;

      component.confirm();

      expect(component.error).toBe(
        'Please type the exact candidate number and confirm that you understand this is permanent.'
      );
      expect(candidateService.eraseCandidate).not.toHaveBeenCalled();
      expect(activeModal.close).not.toHaveBeenCalled();
    });

    it('should call eraseCandidate and close modal on success', () => {
      const response: EraseCandidateResponse = {
        id: 123,
        candidateNumber: '909',
        status: 'deleted',
        erased: true
      };

      component.confirmationCandidateNumber = ' 909 ';
      component.irreversibleConfirmed = true;

      candidateService.eraseCandidate.and.returnValue(of(response));

      component.confirm();

      const expectedRequest: EraseCandidateRequest = {
        confirmationCandidateNumber: '909'
      };

      expect(component.erasing).toBeFalse();
      expect(component.error).toBeNull();
      expect(candidateService.eraseCandidate).toHaveBeenCalledOnceWith(
        123,
        expectedRequest
      );
      expect(activeModal.close).toHaveBeenCalledOnceWith(response);
    });

    it('should stop erasing and set error when service fails', () => {
      component.confirmationCandidateNumber = '909';
      component.irreversibleConfirmed = true;

      candidateService.eraseCandidate.and.returnValue(
        throwError('Backend error')
      );

      component.confirm();

      expect(component.erasing).toBeFalse();
      expect(component.error).toBe('Backend error');
      expect(activeModal.close).not.toHaveBeenCalled();
    });
  });

  describe('modalActionText', () => {
    it('should return Continue on step 1', () => {
      component.step = 1;

      expect(component.modalActionText()).toBe('Continue');
    });

    it('should return Erase candidate data on step 2', () => {
      component.step = 2;

      expect(component.modalActionText()).toBe('Erase candidate data');
    });
  });

  describe('modalActionDisabled', () => {
    it('should return true when erasing', () => {
      component.erasing = true;

      expect(component.modalActionDisabled()).toBeTrue();
    });

    it('should return false on step 1 when not erasing', () => {
      component.step = 1;
      component.erasing = false;

      expect(component.modalActionDisabled()).toBeFalse();
    });

    it('should return true on step 2 when confirmation is invalid', () => {
      component.step = 2;
      component.erasing = false;
      component.confirmationCandidateNumber = 'wrong';
      component.irreversibleConfirmed = true;

      expect(component.modalActionDisabled()).toBeTrue();
    });

    it('should return false on step 2 when confirmation is valid', () => {
      component.step = 2;
      component.erasing = false;
      component.confirmationCandidateNumber = '909';
      component.irreversibleConfirmed = true;

      expect(component.modalActionDisabled()).toBeFalse();
    });
  });

  describe('onModalAction', () => {
    it('should go to next step when current step is less than 2', () => {
      component.step = 1;

      component.onModalAction();

      expect(component.step).toBe(2);
    });

    it('should confirm when current step is 2', () => {
      const response: EraseCandidateResponse = {
        id: 123,
        candidateNumber: '909',
        status: 'deleted',
        erased: true
      };

      component.step = 2;
      component.confirmationCandidateNumber = '909';
      component.irreversibleConfirmed = true;

      candidateService.eraseCandidate.and.returnValue(of(response));

      component.onModalAction();

      expect(candidateService.eraseCandidate).toHaveBeenCalled();
      expect(activeModal.close).toHaveBeenCalledOnceWith(response);
    });
  });
});
