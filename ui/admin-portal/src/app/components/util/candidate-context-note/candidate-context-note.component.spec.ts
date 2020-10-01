import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateContextNoteComponent} from './candidate-context-note.component';

describe('CandidateContextNoteComponent', () => {
  let component: CandidateContextNoteComponent;
  let fixture: ComponentFixture<CandidateContextNoteComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateContextNoteComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateContextNoteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
