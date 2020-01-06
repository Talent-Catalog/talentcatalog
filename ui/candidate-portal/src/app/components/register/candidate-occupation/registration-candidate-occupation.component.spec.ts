import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationCandidateOccupationComponent} from './registration-candidate-occupation.component';

describe('RegistrationCandidateOccupationComponent', () => {
  let component: RegistrationCandidateOccupationComponent;
  let fixture: ComponentFixture<RegistrationCandidateOccupationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationCandidateOccupationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationCandidateOccupationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
