import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateColumnSelectorComponent} from './candidate-column-selector.component';

describe('CandidateColumnSelectorComponent', () => {
  let component: CandidateColumnSelectorComponent;
  let fixture: ComponentFixture<CandidateColumnSelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateColumnSelectorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateColumnSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
