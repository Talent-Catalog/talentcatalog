/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ShowCandidatesComponent } from "./show-candidates.component";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CommonModule, DatePipe, TitleCasePipe} from "@angular/common";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import {NgbModal, NgbPaginationModule, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import { LocalStorageModule, LocalStorageService } from "angular-2-local-storage";
import { SavedSearchService } from "../../../services/saved-search.service";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {
  CandidateSourceDescriptionComponent
} from "../../util/candidate-source-description/candidate-source-description.component";
import {AutosaveStatusComponent} from "../../util/autosave-status/autosave-status.component";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {MockSavedList} from "../../../MockData/MockSavedList";

fdescribe('CandidateShowComponent', () => {
  let component: ShowCandidatesComponent;
  let fixture: ComponentFixture<ShowCandidatesComponent>;
  let formBuilder: FormBuilder;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ShowCandidatesComponent, SortedByComponent, AutosaveStatusComponent,CandidateSourceDescriptionComponent],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        LocalStorageModule.forRoot({}),
        NgbTypeaheadModule,
        NgbPaginationModule,
        ReactiveFormsModule
      ],
      providers: [
        FormBuilder,
        DatePipe,
        TitleCasePipe,
        NgbModal,
        SavedSearchService, // Add any other services that are directly injected into the component
        LocalStorageService,
      ]
    })
    .compileComponents();
  });


  beforeEach(() => {
    fixture = TestBed.createComponent(ShowCandidatesComponent);
    component = fixture.componentInstance;
    // Inject the FormBuilder
    formBuilder = TestBed.inject(FormBuilder);
    // Mock candidate data
    const mockCandidate = {
      taskAssignments: [{ status: 'active' }, { status: 'completed' }] // Example task assignments
    };
    // Initialize the formGroup
    component.searchForm = formBuilder.group({
      keyword: [''],
      showClosedOpps: [false],
      statusesDisplay: [[]]
    });
    component.candidateSource =  new MockCandidateSource();
    component.currentCandidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
