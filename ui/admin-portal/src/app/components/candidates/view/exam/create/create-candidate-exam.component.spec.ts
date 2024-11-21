import {ComponentFixture, TestBed} from '@angular/core/testing';
import {CreateCandidateExamComponent} from './create-candidate-exam.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbActiveModal, NgbModalModule} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('CreateCandidateExamComponent', () => {
  let component: CreateCandidateExamComponent;
  let fixture: ComponentFixture<CreateCandidateExamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateCandidateExamComponent ],
      imports: [HttpClientTestingModule, NgbModalModule,ReactiveFormsModule,NgSelectModule],
      providers: [NgbActiveModal,UntypedFormBuilder],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateExamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
