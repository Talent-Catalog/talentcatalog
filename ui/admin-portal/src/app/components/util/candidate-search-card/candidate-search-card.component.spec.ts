import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateSearchCardComponent} from './candidate-search-card.component';

describe('CandidateSearchCardComponent', () => {
  let component: CandidateSearchCardComponent;
  let fixture: ComponentFixture<CandidateSearchCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateSearchCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateSearchCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
