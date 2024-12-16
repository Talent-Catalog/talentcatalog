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
import {CandidateAdditionalInfoTabComponent} from "./candidate-additional-info-tab.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {ViewCandidateSurveyComponent} from "../../survey/view-candidate-survey.component";
import {
  ViewCandidateMediaWillingnessComponent
} from "../../media/view-candidate-media-willingness.component";
import {
  ViewCandidateSpecialLinksComponent
} from "../../special-links/view-candidate-special-links.component";
import {
  ViewCandidateAttachmentComponent
} from "../../attachment/view-candidate-attachment.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('CandidateAdditionalInfoTabComponent', () => {
  let component: CandidateAdditionalInfoTabComponent;
  let fixture: ComponentFixture<CandidateAdditionalInfoTabComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [ CandidateAdditionalInfoTabComponent,ViewCandidateSurveyComponent,ViewCandidateSurveyComponent,
        ViewCandidateMediaWillingnessComponent,ViewCandidateSpecialLinksComponent,ViewCandidateAttachmentComponent ],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateAdditionalInfoTabComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
