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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {CandidateExamCardComponent} from './candidate-exam-card.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Exam} from '../../../../../model/candidate';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {CandidateExamService} from '../../../../../services/candidate-exam.service';
import {of, throwError} from 'rxjs';
import {By} from '@angular/platform-browser';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('CandidateExamCardComponent', () => {
  let component: CandidateExamCardComponent;
  let fixture: ComponentFixture<CandidateExamCardComponent>;
  let candidateExamService: jasmine.SpyObj<CandidateExamService>;

  beforeEach(async () => {
    const candidateExamServiceSpy = jasmine.createSpyObj('CandidateExamService', ['delete']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      declarations: [CandidateExamCardComponent,AutosaveStatusComponent],
      providers: [
        { provide: CandidateExamService, useValue: candidateExamServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    candidateExamService = TestBed.inject(CandidateExamService) as jasmine.SpyObj<CandidateExamService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateExamCardComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      candidateExams: [
        { id: 1, exam: Exam.TOEFL, score: '7.5', year: 2020, notes: 'Some notes', otherExam: null }
      ]
    };
    component.myRecordIndex = 0;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate exam data', () => {
    expect(component.form.value).toEqual({
      examId: 1,
      examType: 'TOEFL',
      otherExam: null,
      examScore: '7.5',
      examYear: 2020,
      examNotes: 'Some notes'
    });
  });

  it('should display other exam input when exam type is "Other"', () => {
    component.form.controls['examType'].setValue('Other');
    fixture.detectChanges();

    const otherExamInput = fixture.debugElement.query(By.css('input[id^="otherExam"]'));
    expect(otherExamInput).toBeTruthy();
  });

  it('should not display other exam input when exam type is not "Other"', () => {
    component.form.controls['examType'].setValue('IELTS');
    fixture.detectChanges();

    const otherExamInput = fixture.debugElement.query(By.css('input[id^="otherExam"]'));
    expect(otherExamInput).toBeFalsy();
  });

  it('should call delete service and emit delete event on delete', () => {
    spyOn(component.delete, 'emit');
    candidateExamService.delete.and.returnValue(of());

    component.doDelete();

    expect(candidateExamService.delete).toHaveBeenCalledWith(1);
    expect(component.delete.emit).toHaveBeenCalled();
  });

  it('should handle delete service error', () => {
    candidateExamService.delete.and.returnValue(throwError('error'));

    component.doDelete();

    expect(component.error).toBe('error');
  });

  it('should display exam details when an exam is selected', () => {
    const examScoreInput = fixture.debugElement.query(By.css('app-ielts-score-validation'));
    const examYearSelect = fixture.debugElement.query(By.css('ng-select[id^="examYear"]'));
    const examNotesTextarea = fixture.debugElement.query(By.css('textarea[id^="examNotes"]'));

    expect(examScoreInput).toBeTruthy();
    expect(examYearSelect).toBeTruthy();
    expect(examNotesTextarea).toBeTruthy();
  });
});
