import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCandidateDestinationsComponent} from './edit-candidate-destinations.component';

describe('EditCandidateDestinationsComponent', () => {
  let component: EditCandidateDestinationsComponent;
  let fixture: ComponentFixture<EditCandidateDestinationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditCandidateDestinationsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateDestinationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
