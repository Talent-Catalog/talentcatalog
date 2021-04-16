import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidateStatusSelectorComponent } from './candidate-status-selector.component';

describe('CandidateStatusSelectorComponent', () => {
  let component: CandidateStatusSelectorComponent;
  let fixture: ComponentFixture<CandidateStatusSelectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateStatusSelectorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateStatusSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
