import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewCandidateAspirationsComponent } from './view-candidate-aspirations.component';

describe('ViewCandidateAspirationsComponent', () => {
  let component: ViewCandidateAspirationsComponent;
  let fixture: ComponentFixture<ViewCandidateAspirationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewCandidateAspirationsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ViewCandidateAspirationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
