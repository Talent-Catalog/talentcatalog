import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateHistoryTabComponent} from './candidate-history-tab.component';

describe('CandidateHistoryTabComponent', () => {
  let component: CandidateHistoryTabComponent;
  let fixture: ComponentFixture<CandidateHistoryTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateHistoryTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateHistoryTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
