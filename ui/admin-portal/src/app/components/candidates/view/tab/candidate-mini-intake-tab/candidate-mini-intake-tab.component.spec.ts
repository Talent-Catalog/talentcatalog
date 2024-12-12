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

import {CandidateMiniIntakeTabComponent} from "./candidate-mini-intake-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {AuthenticationService} from "../../../../../services/authentication.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {CandidateCitizenshipService} from "../../../../../services/candidate-citizenship.service";
import {CandidateExamService} from "../../../../../services/candidate-exam.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../../MockData/MockCandidate";

describe('CandidateMiniIntakeTabComponent', () => {
  let component: CandidateMiniIntakeTabComponent;
  let fixture: ComponentFixture<CandidateMiniIntakeTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [CandidateMiniIntakeTabComponent],
      providers: [
        CandidateService,
        CountryService,
        EducationLevelService,
        OccupationService,
        LanguageLevelService,
        CandidateNoteService,
        AuthenticationService,
        NgbModal,
        AuthorizationService,
        CandidateCitizenshipService,
        CandidateExamService
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateMiniIntakeTabComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should correctly determine the mini intake completion status', () => {
    // Case 1: miniIntakeCompletedDate is null
    component.candidate = { miniIntakeCompletedDate: null } as any;
    expect(component.miniIntakeComplete).toBeFalse();

    // Case 2: miniIntakeCompletedDate is not null
    component.candidate = { miniIntakeCompletedDate: new Date() } as any;
    expect(component.miniIntakeComplete).toBeTrue();
  });
});
