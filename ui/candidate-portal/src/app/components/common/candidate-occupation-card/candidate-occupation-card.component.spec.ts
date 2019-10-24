import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateOccupationCardComponent} from './candidate-occupation-card.component';

describe('CandidateOccupationCardComponent', () => {
  let component: CandidateOccupationCardComponent;
  let fixture: ComponentFixture<CandidateOccupationCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateOccupationCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOccupationCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
