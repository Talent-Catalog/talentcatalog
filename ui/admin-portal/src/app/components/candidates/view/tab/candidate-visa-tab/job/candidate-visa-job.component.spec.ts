import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateVisaJobComponent} from './candidate-visa-job.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";

describe('CandidateVisaJobComponent', () => {
  let component: CandidateVisaJobComponent;
  let fixture: ComponentFixture<CandidateVisaJobComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,LocalStorageModule.forRoot({})],
      declarations: [ CandidateVisaJobComponent ],
      providers: [
        FormBuilder
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateVisaJobComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
