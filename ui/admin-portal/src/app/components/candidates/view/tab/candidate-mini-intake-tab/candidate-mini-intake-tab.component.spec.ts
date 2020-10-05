import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateMiniIntakeTabComponent} from './candidate-mini-intake-tab.component';

describe('CandidateMiniIntakeTabComponent', () => {
  let component: CandidateMiniIntakeTabComponent;
  let fixture: ComponentFixture<CandidateMiniIntakeTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateMiniIntakeTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateMiniIntakeTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
