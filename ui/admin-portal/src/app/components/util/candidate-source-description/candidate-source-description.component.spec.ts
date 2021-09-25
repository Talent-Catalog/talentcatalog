import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidateSourceDescriptionComponent } from './candidate-source-description.component';

describe('CandidateSourceDescriptionComponent', () => {
  let component: CandidateSourceDescriptionComponent;
  let fixture: ComponentFixture<CandidateSourceDescriptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateSourceDescriptionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateSourceDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
