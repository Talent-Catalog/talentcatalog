import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateCertificationCardComponent} from './candidate-certification-card.component';

describe('CandidateCertificationCardComponent', () => {
  let component: CandidateCertificationCardComponent;
  let fixture: ComponentFixture<CandidateCertificationCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateCertificationCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateCertificationCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
