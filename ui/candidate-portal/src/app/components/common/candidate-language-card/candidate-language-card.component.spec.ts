import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateLanguageCardComponent} from './candidate-language-card.component';

describe('CandidateLanguageCardComponent', () => {
  let component: CandidateLanguageCardComponent;
  let fixture: ComponentFixture<CandidateLanguageCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateLanguageCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateLanguageCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
