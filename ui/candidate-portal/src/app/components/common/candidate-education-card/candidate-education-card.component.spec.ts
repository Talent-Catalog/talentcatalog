import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateEducationCardComponent} from './candidate-education-card.component';

describe('CandidateEducationCardComponent', () => {
  let component: CandidateEducationCardComponent;
  let fixture: ComponentFixture<CandidateEducationCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateEducationCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateEducationCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
