import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationJobExperienceComponent} from './registration-job-experience.component';

describe('RegistrationJobExperienceComponent', () => {
  let component: RegistrationJobExperienceComponent;
  let fixture: ComponentFixture<RegistrationJobExperienceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationJobExperienceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationJobExperienceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
