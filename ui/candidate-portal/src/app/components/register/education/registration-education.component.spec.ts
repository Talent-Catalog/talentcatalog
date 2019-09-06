import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationEducationComponent} from './registration-education.component';

describe('RegistrationEducationComponent', () => {
  let component: RegistrationEducationComponent;
  let fixture: ComponentFixture<RegistrationEducationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RegistrationEducationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationEducationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
