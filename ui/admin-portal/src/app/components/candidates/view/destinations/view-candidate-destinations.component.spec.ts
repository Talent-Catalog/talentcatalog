import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateDestinationsComponent} from './view-candidate-destinations.component';

describe('ViewCandidateDestinationsComponent', () => {
  let component: ViewCandidateDestinationsComponent;
  let fixture: ComponentFixture<ViewCandidateDestinationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateDestinationsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateDestinationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
