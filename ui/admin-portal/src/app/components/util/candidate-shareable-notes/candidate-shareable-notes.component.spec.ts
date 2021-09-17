import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidateShareableNotesComponent } from './candidate-shareable-notes.component';

describe('CandidateShareableNotesComponent', () => {
  let component: CandidateShareableNotesComponent;
  let fixture: ComponentFixture<CandidateShareableNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateShareableNotesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateShareableNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
