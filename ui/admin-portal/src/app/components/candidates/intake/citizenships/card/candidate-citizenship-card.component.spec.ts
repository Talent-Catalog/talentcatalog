import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateCitizenshipCardComponent} from './candidate-citizenship-card.component';

describe('CandidateCitizenshipCardComponent', () => {
  let component: CandidateCitizenshipCardComponent;
  let fixture: ComponentFixture<CandidateCitizenshipCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateCitizenshipCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateCitizenshipCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
