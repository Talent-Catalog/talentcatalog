import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateVisaJobAssessementComponent} from './create-visa-job-assessement.component';

describe('CreateVisaJobAssessementComponent', () => {
  let component: CreateVisaJobAssessementComponent;
  let fixture: ComponentFixture<CreateVisaJobAssessementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateVisaJobAssessementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateVisaJobAssessementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
