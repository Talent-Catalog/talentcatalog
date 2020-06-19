import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCandidateOccupationComponent } from './create-candidate-occupation.component';

describe('CreateCandidateOccupationComponent', () => {
  let component: CreateCandidateOccupationComponent;
  let fixture: ComponentFixture<CreateCandidateOccupationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateCandidateOccupationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateOccupationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
