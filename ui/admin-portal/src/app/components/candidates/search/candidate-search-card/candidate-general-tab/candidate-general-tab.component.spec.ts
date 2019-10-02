import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateGeneralTabComponent} from './candidate-general-tab.component';

describe('CandidateGeneralTabComponent', () => {
  let component: CandidateGeneralTabComponent;
  let fixture: ComponentFixture<CandidateGeneralTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateGeneralTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateGeneralTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
