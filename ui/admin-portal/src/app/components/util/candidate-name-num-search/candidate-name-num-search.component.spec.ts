import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateNameNumSearchComponent} from './candidate-name-num-search.component';

describe('CandidateNameNumSearchComponent', () => {
  let component: CandidateNameNumSearchComponent;
  let fixture: ComponentFixture<CandidateNameNumSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateNameNumSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateNameNumSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
