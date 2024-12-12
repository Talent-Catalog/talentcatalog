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
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {EditCandidateSurveyComponent} from './edit-candidate-survey.component';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateService} from '../../../../../services/candidate.service';
import {SurveyTypeService} from '../../../../../services/survey-type.service';
import {of, throwError} from 'rxjs';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {NgSelectModule} from "@ng-select/ng-select";

describe('EditCandidateSurveyComponent', () => {
  let component: EditCandidateSurveyComponent;
  let fixture: ComponentFixture<EditCandidateSurveyComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let surveyTypeService: jasmine.SpyObj<SurveyTypeService>;

  const candidate = new MockCandidate();

  const surveyTypes = [
    {id: 1, name: 'Online Search'},
    {id: 2, name: 'Job Board'},
    {id: 3, name: 'Referral'},
    {id: 4, name: 'Social Media'},
    {id: 5, name: 'Other'}
  ];

  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get', 'updateSurvey']);
    const surveyTypeServiceSpy = jasmine.createSpyObj('SurveyTypeService', ['listSurveyTypes']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateSurveyComponent],
      imports: [ReactiveFormsModule, HttpClientTestingModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        NgbActiveModal,
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: SurveyTypeService, useValue: surveyTypeServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    surveyTypeService = TestBed.inject(SurveyTypeService) as jasmine.SpyObj<SurveyTypeService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateSurveyComponent);
    component = fixture.componentInstance;
    component.candidateId = candidate.id;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with correct values', fakeAsync(() => {
    candidateService.get.and.returnValue(of(candidate));
    surveyTypeService.listSurveyTypes.and.returnValue(of(surveyTypes));

    component.ngOnInit();
    tick(); // Simulate the asynchronous passage of time

    fixture.detectChanges();

    expect(component.candidateForm).toBeDefined();
    expect(component.candidateForm.get('surveyTypeId').value).toBe(candidate.surveyType.id);
    expect(component.candidateForm.get('surveyComment').value).toBe(candidate.surveyComment);
    expect(component.loading).toBeFalse();
  }));

});
