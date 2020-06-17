import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteCandidateOccupationComponent } from './delete-candidate-occupation.component';

describe('DeleteCandidateOccupationComponent', () => {
  let component: DeleteCandidateOccupationComponent;
  let fixture: ComponentFixture<DeleteCandidateOccupationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteCandidateOccupationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteCandidateOccupationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
