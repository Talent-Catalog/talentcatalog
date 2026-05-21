import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditCandidateAspirationsComponent } from './edit-candidate-aspirations.component';

describe('EditCandidateAspirationsComponent', () => {
  let component: EditCandidateAspirationsComponent;
  let fixture: ComponentFixture<EditCandidateAspirationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditCandidateAspirationsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EditCandidateAspirationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
