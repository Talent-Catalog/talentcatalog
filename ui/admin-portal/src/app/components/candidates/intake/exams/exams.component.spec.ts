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
import {ExamsComponent} from './exams.component';
import {CandidateExamService} from '../../../../services/candidate-exam.service';
import {CandidateIntakeData, Exam} from '../../../../model/candidate';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
describe('ExamsComponent', () => {
  let component: ExamsComponent;
  let fixture: ComponentFixture<ExamsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      declarations: [ExamsComponent,AutosaveStatusComponent],
      providers: [
        { provide: CandidateExamService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExamsComponent);
    component = fixture.componentInstance;

    component.candidate = new MockCandidate();
    component.candidateIntakeData = {
      candidateExams: []
    } as CandidateIntakeData;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display "No Exams Added" message when no exams are present', () => {
    component.candidateIntakeData.candidateExams = [];
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('p')?.textContent).toContain('No Exams Added');
  });

  it('should display exams when exams are present', () => {
    component.candidateIntakeData.candidateExams = [
      { id: 1, exam: Exam.TOEFL, score: '90' },
      { id: 2, exam: Exam.IELTSGen, score: '85' }
    ];
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelectorAll('app-candidate-exam-card').length).toBe(2);
  });

  it('should delete an exam when deleteRecord is called', () => {
    component.candidateIntakeData.candidateExams = [
      { id: 1, exam: Exam.TOEFL, score: '90' },
      { id: 2, exam: Exam.IELTSGen, score: '85' }
    ];
    fixture.detectChanges();

    component.deleteRecord(0);
    fixture.detectChanges();

    expect(component.candidateIntakeData.candidateExams.length).toBe(1);
    expect(component.candidateIntakeData.candidateExams[0].exam).toBe(Exam.IELTSGen);
  });
});
