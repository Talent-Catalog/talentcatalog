import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateVisaTabComponent} from './candidate-visa-tab.component';

describe('CandidateVisaTabComponent', () => {
  let component: CandidateVisaTabComponent;
  let fixture: ComponentFixture<CandidateVisaTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateVisaTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateVisaTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
