import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditCandidateExamComponent } from './edit-candidate-exam.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbActiveModal, NgbModalModule} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";

describe('EditCandidateExamComponent', () => {
  let component: EditCandidateExamComponent;
  let fixture: ComponentFixture<EditCandidateExamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditCandidateExamComponent ],
      imports: [HttpClientTestingModule, NgbModalModule,ReactiveFormsModule,NgSelectModule],
      providers: [NgbActiveModal,FormBuilder],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateExamComponent);
    component = fixture.componentInstance;
    component.candidateExam = new MockCandidate().candidateExams[0];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
