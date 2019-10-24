import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateEducationFormComponent} from './candidate-education-form.component';

describe('RegistrationMastersComponent', () => {
  let component: CandidateEducationFormComponent;
  let fixture: ComponentFixture<CandidateEducationFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateEducationFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateEducationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
