/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
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
import {vi} from 'vitest';
import {WorkLegallyComponent} from './work-legally.component';
import {CandidateService} from '../../../../services/candidate.service';
import {CrossTabSyncService} from '../../../../services/cross-tab-sync.service';
import {YesNo} from '../../../../model/candidate';
import {enumOptions} from '../../../../util/enum';

describe('WorkLegallyComponent', () => {
  let component: WorkLegallyComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UntypedFormBuilder,
        {provide: CandidateService, useValue: {updateIntakeData: vi.fn()}},
        {provide: CrossTabSyncService, useValue: {broadcastCandidateUpdated: vi.fn()}}
      ]
    });
    component = TestBed.runInInjectionContext(() => new WorkLegallyComponent(
      TestBed.inject(UntypedFormBuilder),
      TestBed.inject(CandidateService)
    ));
  });

  it('exposes work-legally options for the YesNo enum', () => {
    expect(component.workLegallyOptions).toEqual(enumOptions(YesNo));
  });

  it('initializes without throwing', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });
});
