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
import {ReturnHomeFutureComponent} from "./return-home-future.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../../services/candidate.service";
import {NgSelectModule} from "@ng-select/ng-select";
import {ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {YesNoUnsure} from "../../../../model/candidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ReturnHomeFutureComponent', () => {
  let component: ReturnHomeFutureComponent;
  let fixture: ComponentFixture<ReturnHomeFutureComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReturnHomeFutureComponent, AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReturnHomeFutureComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      returnHomeFuture: YesNoUnsure.Yes,
      returnHomeWhen: '2023-12-01'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('returnHomeFuture').value).toBe(YesNoUnsure.Yes);
    expect(component.form.get('returnHomeWhen').value).toBe('2023-12-01');
  });

  it('should display the return home date input field when returnHomeFuture is Yes', () => {
    component.form.patchValue({ returnHomeFuture: 'Yes' });
    fixture.detectChanges();
    const returnHomeWhenInput = fixture.nativeElement.querySelector('#returnHomeWhen');
    expect(returnHomeWhenInput).toBeTruthy();
  });

  it('should not display the return home date input field when returnHomeFuture is not Yes', () => {
    component.form.patchValue({ returnHomeFuture: 'No' });
    fixture.detectChanges();
    const returnHomeWhenInput = fixture.nativeElement.querySelector('#returnHomeWhen');
    expect(returnHomeWhenInput).toBeFalsy();
  });
});
