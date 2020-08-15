import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidateCertificationFormComponent } from './candidate-certification-form.component';

describe('CandidateCertificationFormComponent', () => {
  let component: CandidateCertificationFormComponent;
  let fixture: ComponentFixture<CandidateCertificationFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateCertificationFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateCertificationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
