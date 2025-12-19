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
import {ViewCandidateSurveyComponent} from "./view-candidate-survey.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {By} from "@angular/platform-browser";
import {CUSTOM_ELEMENTS_SCHEMA, DebugElement} from "@angular/core";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ViewCandidateSurveyComponent', () => {
  let component: ViewCandidateSurveyComponent;
  let fixture: ComponentFixture<ViewCandidateSurveyComponent>;

  const candidate = new MockCandidate();
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidate']);
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateSurveyComponent ],
      providers: [
        { provide: CandidateService, userValue: candidateServiceSpy }
      ],
      imports: [NgbNavModule, HttpClientTestingModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateSurveyComponent);
    component = fixture.componentInstance;
    component.candidate = candidate;
    component.editable = true; // Assume component is editable for testing the button visibility
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display candidate survey source', () => {
    const sourceEl: HTMLElement = fixture.debugElement.query(By.css('[appHighlightSearch]')).nativeElement;
    expect(sourceEl.textContent).toContain('Survey');
  });

  it('should display candidate survey comment', () => {
    const commentEl: HTMLElement = fixture.debugElement.queryAll(By.css('[appHighlightSearch]'))[1].nativeElement;
    expect(commentEl.textContent).toContain('Referred by a friend');
  });

  it('should display the edit button when editable is true', () => {
    const editButton = fixture.nativeElement.querySelector('tc-card-header tc-button');
    expect(editButton).toBeTruthy();
  });

  it('should not display the edit button when editable is false', () => {
    component.editable = false;
    fixture.detectChanges();
    const buttonEl: DebugElement = fixture.debugElement.query(By.css('.btn-secondary'));
    expect(buttonEl).toBeNull();
  });
});
