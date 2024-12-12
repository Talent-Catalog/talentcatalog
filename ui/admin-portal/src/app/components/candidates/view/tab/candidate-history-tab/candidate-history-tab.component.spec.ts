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

import {CandidateHistoryTabComponent} from "./candidate-history-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {ViewCandidateNoteComponent} from "../../note/view-candidate-note.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";

describe('CandidateHistoryTabComponent', () => {
  let component: CandidateHistoryTabComponent;
  let fixture: ComponentFixture<CandidateHistoryTabComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [ CandidateHistoryTabComponent,ViewCandidateNoteComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateHistoryTabComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with null error', () => {
    expect(component.error).toBeNull();
  });

  it('should trigger onResize event when resize method is called', () => {
    spyOn(component.onResize, 'emit');
    component.resize();
    expect(component.onResize.emit).toHaveBeenCalled();
  });

  it('should not change loading state when candidate data changes', () => {
    const candidate = mockCandidate;

    component.loading = false;
    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: candidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });
    expect(component.loading).toBe(false);
  });

  it('should update result with candidate data when data changes', () => {
    const candidate = mockCandidate

    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: candidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });
    expect(component.result).toEqual(candidate);
  });

});
