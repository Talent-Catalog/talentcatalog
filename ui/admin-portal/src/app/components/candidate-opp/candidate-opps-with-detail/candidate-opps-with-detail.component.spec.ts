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

import {CandidateOppsWithDetailComponent} from "./candidate-opps-with-detail.component";
import {ComponentFixture, fakeAsync, TestBed} from "@angular/core/testing";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {SearchOppsBy} from "../../../model/base";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {CandidateOppsComponent} from "../candidate-opps/candidate-opps.component";

describe('CandidateOppsWithDetailComponent', () => {
  let component: CandidateOppsWithDetailComponent;
  let fixture: ComponentFixture<CandidateOppsWithDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CandidateOppsWithDetailComponent,CandidateOppsComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,FormsModule,CommonModule],

    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppsWithDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values and configurations', () => {
    // Check if component initializes with default values
    expect(component.error).toBeUndefined();
    expect(component.loading).toBeFalsy();
    expect(component.selectedOpp).toBeUndefined();
  });

  it('should assign selected opportunity when a candidate opportunity is selected', () => {
    const mockOpp: CandidateOpportunity = mockCandidateOpportunity;

    component.onOppSelected(mockOpp);

    expect(component.selectedOpp).toEqual(mockOpp);
  });

  it('should propagate update event appropriately when an opportunity is updated', () => {
    // Mock candidate opportunity
    const mockOpp: CandidateOpportunity = mockCandidateOpportunity;
    component.candidateOpps = [mockOpp];
    // Spy on emit method of candidateOppUpdated EventEmitter
    spyOn(component.candidateOppUpdated, 'emit');
    // Call onCandidateOppUpdated with the mock opportunity
    component.onCandidateOppUpdated(mockOpp);
    // If candidateOpps is defined, it should emit the updated opportunity
    expect(component.candidateOppUpdated.emit).toHaveBeenCalledWith(mockOpp);
    spyOn(component.candidateOppsComponent, 'search');

    component.candidateOpps = null;
    // If searchBy is defined, it should call the search method of the child component
    component.searchBy = SearchOppsBy.starredByMe;
    component.onCandidateOppUpdated(mockOpp);
    expect(component.candidateOppsComponent.search).toHaveBeenCalled();
  });
});
