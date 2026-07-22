/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {TestBed} from '@angular/core/testing';
import {UntypedFormBuilder} from '@angular/forms';
import {of, Subject} from 'rxjs';

import {IntakeComponentBase} from './IntakeComponentBase';
import {CandidateService} from '../../../services/candidate.service';
import {CrossTabSyncService} from '../../../services/cross-tab-sync.service';

class TestIntakeComponent extends IntakeComponentBase {
  constructor(fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      status: [''],
      options: [[]]
    });
  }
}

describe('IntakeComponentBase', () => {
  let component: TestIntakeComponent;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let crossTabSyncService: jasmine.SpyObj<CrossTabSyncService>;

  beforeEach(() => {
    candidateService = jasmine.createSpyObj<CandidateService>(
      'CandidateService',
      ['updateIntakeData']
    );
    candidateService.updateIntakeData.and.returnValue(of(void 0));

    const candidateUpdatedSubject =
      new Subject<{id: number; ts: number}>();

    crossTabSyncService = jasmine.createSpyObj<CrossTabSyncService>(
      'CrossTabSyncService',
      ['broadcastCandidateUpdated'],
      {
        candidateUpdated$: candidateUpdatedSubject.asObservable()
      }
    );

    TestBed.configureTestingModule({
      providers: [
        UntypedFormBuilder,
        {provide: CandidateService, useValue: candidateService},
        {provide: CrossTabSyncService, useValue: crossTabSyncService}
      ]
    });

    TestBed.runInInjectionContext(() => {
      component = new TestIntakeComponent(
        TestBed.inject(UntypedFormBuilder),
        candidateService
      );
    });

    component.ngOnInit();
    component.entity = {id: 10} as any;
    component.candidateIntakeData = {} as any;
  });

  it('should return entity as candidate', () => {
    expect(component.candidate).toBe(component.entity as any);
  });

  it('should convert selected enum options to enum keys', () => {
    const formValue = {
      options: [
        {key: 'ONE', stringValue: 'One'},
        {key: 'TWO', stringValue: 'Two'}
      ],
      unchanged: 'value'
    };

    const result = component.preprocessFormValues(formValue) as any;

    expect(result).toEqual({
      options: ['ONE', 'TWO'],
      unchanged: 'value'
    });
  });

  it('should broadcast candidate update after successful save', () => {
    component.onSuccessfulSave();

    expect(crossTabSyncService.broadcastCandidateUpdated).toHaveBeenCalledWith(10);
  });

  it('should set NoResponse on a form control', () => {
    component.setNoResponse('status');

    expect(component.form.get('status')?.value).toBe('NoResponse');
  });

  it('should update candidate intake data when a field changes', () => {
    component.updateDataOnFieldChange('status');

    component.form.get('status')?.setValue('Complete');

    expect((component.candidateIntakeData as any).status).toBe('Complete');
  });
});
