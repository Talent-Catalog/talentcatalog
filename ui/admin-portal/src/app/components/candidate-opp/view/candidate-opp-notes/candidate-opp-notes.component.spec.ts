import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateOppNotesComponent} from './candidate-opp-notes.component';

describe('CandidateOppNotesComponent', () => {
  let component: CandidateOppNotesComponent;
  let fixture: ComponentFixture<CandidateOppNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateOppNotesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
