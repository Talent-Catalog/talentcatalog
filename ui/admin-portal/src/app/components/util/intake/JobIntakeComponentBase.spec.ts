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

import {JobIntakeComponentBase} from './JobIntakeComponentBase';
import {JobService} from '../../../services/job.service';

class TestJobIntakeComponent extends JobIntakeComponentBase {
  constructor(fb: UntypedFormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      availability: [''],
      options: [[]]
    });
  }
}

describe('JobIntakeComponentBase', () => {
  let component: TestJobIntakeComponent;
  let jobService: jasmine.SpyObj<JobService>;

  beforeEach(() => {
    jobService = jasmine.createSpyObj<JobService>('JobService', ['updateIntakeData']);
    jobService.updateIntakeData.and.returnValue(of(void 0));

    TestBed.configureTestingModule({
      providers: [
        UntypedFormBuilder,
        {provide: JobService, useValue: jobService}
      ]
    });

    component = new TestJobIntakeComponent(
      TestBed.inject(UntypedFormBuilder),
      jobService
    );
    component.ngOnInit();
    component.entity = {id: 20} as any;
    component.componentKey = 'JOB.INTAKE';
  });

  it('should return entity as job', () => {
    expect(component.job).toBe(component.entity as any);
  });

  it('should build component label key', () => {
    expect(component.componentLabelKey).toBe('JOB.INTAKE.LABEL');
  });

  it('should build component tooltip key', () => {
    expect(component.componentTooltipKey).toBe('JOB.INTAKE.TOOLTIP');
  });

  it('should convert enum options to enum keys', () => {
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

  it('should set NoResponse on a form control', () => {
    component.setNoResponse('availability');

    expect(component.form.get('availability')?.value).toBe('NoResponse');
  });
});
