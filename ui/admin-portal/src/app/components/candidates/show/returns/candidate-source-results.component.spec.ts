import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateSourceResultsComponent} from './candidate-source-results.component';

describe('ReturnsComponent', () => {
  let component: CandidateSourceResultsComponent;
  let fixture: ComponentFixture<CandidateSourceResultsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateSourceResultsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateSourceResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
