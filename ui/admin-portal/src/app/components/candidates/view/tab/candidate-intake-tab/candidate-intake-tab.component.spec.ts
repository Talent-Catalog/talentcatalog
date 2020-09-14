import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateIntakeTabComponent} from './candidate-intake-tab.component';

describe('CandidateIntakeTabComponent', () => {
  let component: CandidateIntakeTabComponent;
  let fixture: ComponentFixture<CandidateIntakeTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateIntakeTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateIntakeTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
