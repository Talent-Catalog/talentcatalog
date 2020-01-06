import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateAdditionalInfoTabComponent} from './candidate-additional-info-tab.component';

describe('CandidateAdditionalInfoTabComponent', () => {
  let component: CandidateAdditionalInfoTabComponent;
  let fixture: ComponentFixture<CandidateAdditionalInfoTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateAdditionalInfoTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateAdditionalInfoTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
