import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidatesSearchComponent} from './candidates-search.component';

describe('CandidatesSearchComponent', () => {
  let component: CandidatesSearchComponent;
  let fixture: ComponentFixture<CandidatesSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidatesSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidatesSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
