import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationWorkExperienceComponent} from './registration-work-experience.component';

describe('RegistrationWorkExperienceComponent', () => {
  let component: RegistrationWorkExperienceComponent;
  let fixture: ComponentFixture<RegistrationWorkExperienceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationWorkExperienceComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationWorkExperienceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
