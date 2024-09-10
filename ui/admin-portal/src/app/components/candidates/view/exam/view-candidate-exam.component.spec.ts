import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewCandidateExamComponent } from './view-candidate-exam.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbActiveModal, NgbModalModule} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('ViewCandidateExamComponent', () => {
  let component: ViewCandidateExamComponent;
  let fixture: ComponentFixture<ViewCandidateExamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateExamComponent ],
      imports: [HttpClientTestingModule, NgbModalModule,ReactiveFormsModule,NgSelectModule],
      providers: [NgbActiveModal,FormBuilder],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateExamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
