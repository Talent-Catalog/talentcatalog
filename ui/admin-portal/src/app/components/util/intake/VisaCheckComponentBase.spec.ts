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
import {of} from 'rxjs';

import {VisaCheckComponentBase} from './VisaCheckComponentBase';
import {CandidateVisaCheckService} from '../../../services/candidate-visa-check.service';

class TestVisaCheckComponent extends VisaCheckComponentBase {
  constructor(
    fb: UntypedFormBuilder,
    candidateVisaCheckService: CandidateVisaCheckService
  ) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      assessment: [''],
      options: [[]]
    });
  }
}

describe('VisaCheckComponentBase', () => {
  let component: TestVisaCheckComponent;
  let service: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(() => {
    service = jasmine.createSpyObj<CandidateVisaCheckService>(
      'CandidateVisaCheckService',
      ['updateIntakeData']
    );
    service.updateIntakeData.and.returnValue(of(void 0));

    TestBed.configureTestingModule({
      providers: [
        UntypedFormBuilder,
        {provide: CandidateVisaCheckService, useValue: service}
      ]
    });

    component = new TestVisaCheckComponent(
      TestBed.inject(UntypedFormBuilder),
      service
    );
    component.ngOnInit();
    component.entity = {id: 30} as any;
  });

  it('should return entity as visa check', () => {
    expect(component.visaCheck).toBe(component.entity as any);
  });

  it('should convert enum options to enum keys', () => {
    const formValue = {
      options: [
        {key: 'ELIGIBLE', stringValue: 'Eligible'},
        {key: 'INELIGIBLE', stringValue: 'Ineligible'}
      ],
      unchanged: 'value'
    };

    const result = component.preprocessFormValues(formValue) as any;

    expect(result).toEqual({
      options: ['ELIGIBLE', 'INELIGIBLE'],
      unchanged: 'value'
    });
  });

  it('should set NoResponse on a form control', () => {
    component.setNoResponse('assessment');

    expect(component.form.get('assessment')?.value).toBe('NoResponse');
  });
});
