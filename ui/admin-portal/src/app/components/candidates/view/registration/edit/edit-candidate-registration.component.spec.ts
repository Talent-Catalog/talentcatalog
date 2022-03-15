import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCandidateRegistrationComponent} from './edit-candidate-registration.component';

describe('EditCandidateRegistrationComponent', () => {
  let component: EditCandidateRegistrationComponent;
  let fixture: ComponentFixture<EditCandidateRegistrationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditCandidateRegistrationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateRegistrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
