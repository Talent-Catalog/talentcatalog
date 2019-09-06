import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationSchoolComponent} from './registration-school.component';

describe('RegistrationSchoolComponent', () => {
  let component: RegistrationSchoolComponent;
  let fixture: ComponentFixture<RegistrationSchoolComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationSchoolComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationSchoolComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
